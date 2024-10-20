package me.vannername.qol.main.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType.getInteger
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import me.fzzyhmstrs.fzzy_config.util.Walkable
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedAny
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt
import me.vannername.qol.QoLMod
import me.vannername.qol.QoLMod.serverConfig
import me.vannername.qol.main.commands.util.ServerCommandHandlerBase
import me.vannername.qol.main.utils.Utils
import me.vannername.qol.main.utils.Utils.appendCommandSuggestion
import me.vannername.qol.main.utils.Utils.multiColored
import me.vannername.qol.main.utils.Utils.sendCommandError
import me.vannername.qol.main.utils.Utils.sentenceCase
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.command.CommandSource
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting

// as register() is fully overwritten, command name is purely decorative
object SkipDayNight : ServerCommandHandlerBase("skipdaynight") {

    // TODO: resolve errors with skip force

    override fun init() {
        Mode.NIGHT.opposite = Mode.DAY
        Mode.DAY.opposite = Mode.NIGHT

        super.init()
        detectTimeChange()
    }

    private enum class SkipDayNightSuggestionProviderKeys : SuggestionProviderKey {
        DURATION;

        override fun key(): String = this.name
    }

    private enum class SkipDayNightCommandNodeKeys : CommandNodeKey {
        ROOT_SKIPDAY,
        ROOT_SKIPNIGHT,
        DURATION,
        INF,
        FORCE,
        STATUS;

        override fun key(): String = this.name
    }

    override fun registerCommandNodes() {
        fun currentMode(ctx: CommandContext<ServerCommandSource>): Mode {
            return if (ctx.input.contains("skipday")) Mode.DAY else Mode.NIGHT
        }

        CommandManager
            .literal("skipday")
            .executes(::help)
            .register(SkipDayNightCommandNodeKeys.ROOT_SKIPDAY)

        CommandManager
            .literal("skipnight")
            .executes(::help)
            .register(SkipDayNightCommandNodeKeys.ROOT_SKIPNIGHT)

        CommandManager
            .literal("help")
            .executes(::help)
            .register(HELP)

        CommandManager
            .argument("duration", IntegerArgumentType.integer())
            .suggests { ctx, builder ->
                SkipDayNightSuggestionProviderKeys.DURATION.getSuggestions(ctx, builder)
            }
            .executes { ctx ->
                skipPeriod(currentMode(ctx), getInteger(ctx, "duration"), false, ctx)
            }
            .register(SkipDayNightCommandNodeKeys.DURATION)

        CommandManager
            .literal("inf")
            .executes { ctx ->
                skipPeriod(currentMode(ctx), 0, true, ctx)
            }
            .register(SkipDayNightCommandNodeKeys.INF)

        CommandManager
            .literal("force")
            .executes { ctx ->
                skipForce(currentMode(ctx), ctx)
            }
            .register(SkipDayNightCommandNodeKeys.FORCE)

        CommandManager
            .literal("status")
            .executes { ctx ->
                showStatus(currentMode(ctx), ctx)
            }
            .register(SkipDayNightCommandNodeKeys.STATUS)
    }

    override fun registerSuggestionProviders() {
        registerSuggestionProvider(SkipDayNightSuggestionProviderKeys.DURATION)
        { ctx, builder ->
            CommandSource.suggestMatching(
                listOf("5", "10", "50"), builder
            )
        }
    }

    override fun commandStructure() {

        SkipDayNightCommandNodeKeys.ROOT_SKIPDAY
            .addChildren(
                HELP,
                SkipDayNightCommandNodeKeys.DURATION,
                SkipDayNightCommandNodeKeys.INF,
                SkipDayNightCommandNodeKeys.FORCE,
                SkipDayNightCommandNodeKeys.STATUS
            )

        SkipDayNightCommandNodeKeys.ROOT_SKIPNIGHT
            .addChildren(
                HELP,
                SkipDayNightCommandNodeKeys.DURATION,
                SkipDayNightCommandNodeKeys.INF,
                SkipDayNightCommandNodeKeys.FORCE,
                SkipDayNightCommandNodeKeys.STATUS
            )
    }

    override fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->

            dispatcher.root.addChild(SkipDayNightCommandNodeKeys.ROOT_SKIPDAY.getNode())
            dispatcher.root.addChild(SkipDayNightCommandNodeKeys.ROOT_SKIPNIGHT.getNode())

            // aliases
            dispatcher.register(literal<ServerCommandSource>("sd").redirect(SkipDayNightCommandNodeKeys.ROOT_SKIPDAY.getNode()))
            dispatcher.register(literal<ServerCommandSource>("sn").redirect(SkipDayNightCommandNodeKeys.ROOT_SKIPNIGHT.getNode()))

            commandStructure()
        }
    }

    override fun defineHelpMessages() {
        addPathDescriptions(
            listOf(SkipDayNightCommandNodeKeys.ROOT_SKIPDAY, HELP) to "Displays this help message.",
            listOf(SkipDayNightCommandNodeKeys.ROOT_SKIPNIGHT, HELP) to "Displays this help message.",
            appendRoot = false
        )
    }

    /**
     * Detect the time change and skip the day/night if necessary.
     * This method is called every tick.
     */
    private fun detectTimeChange() {
        // 12000 (10:00) Beginning of the Minecraft sunset.
        // Villagers go to their beds and sleep.
        // 23460 (19:33) In clear weather, beds can no longer be used.
        // In clear weather, bees leave the nest/hive.
        // In clear weather, undead mobs begin to burn.

        ServerTickEvents.END_WORLD_TICK.register { world ->
            try {
                val mode = if (world.timeOfDay % 24000 in 12000..23459) Mode.NIGHT else Mode.DAY
                if (mode.associatedEntry.get().isSet()) {
                    performSkip(mode)
                }
            } catch (e: IllegalStateException) {
                // Should only be thrown if the period is 0 and skipping is
                // impossible; Do nothing
            }
        }
    }

    @Throws(IllegalStateException::class)
    private fun performSkip(mode: Mode) {
        val currentValue = mode.associatedEntry.get().getAndUpdate()
        val world = QoLMod.defaultWorld!!

        if (!mode.opposite!!.skipForce) {
            // only needed because world.timeOfDay can go beyond 24000
            val fullDays = world.timeOfDay.toInt() / 24000
            // math
            world.timeOfDay = (fullDays + 1) * 24000L + if (mode == Mode.DAY) 13000 else 0
            world.players.forEach {
                it.sendMessage(
                    Text.literal("${mode.name.sentenceCase()} successfully skipped!").formatted(Formatting.AQUA)
                )
                if (currentValue == 1) {
                    it.sendMessage(
                        Text.literal("Warning: this was the last ${mode.name.lowercase()} skip.")
                            .formatted(Formatting.YELLOW)
                    )
                }
            }
        }
    }

    /**
     * A class that represents the mode (day or night) to skip.
     */
    enum class Mode(
        val color: Utils.Colors,
        val associatedEntry: ValidatedAny<SkipPeriod>,
        var opposite: Mode? = null,
        var skipForce: Boolean = false
    ) {
        DAY(Utils.Colors.YELLOW, serverConfig.skippingSettings.daysToSkip),
        NIGHT(Utils.Colors.GRAY, serverConfig.skippingSettings.nightsToSkip)
    }

    /**
     * Set the number of days or nights to skip.
     *
     * @param mode The mode (day or night) to skip.
     * @param duration The number of days or nights to skip.
     * @param isInfinite Whether the period is infinite or not.
     */
    @Throws(CommandSyntaxException::class)
    private fun skipPeriod(
        mode: Mode,
        duration: Int,
        isInfinite: Boolean,
        ctx: CommandContext<ServerCommandSource>
    ): Int {
        if (mode.opposite!!.associatedEntry.get().isSet()) {

            ctx.source.sendMessage(
                Text.literal("${mode.opposite!!.name.lowercase()} skipping has already been set. Use ")
                    .appendCommandSuggestion("/skip${mode.name.lowercase()} force")
                    .append(" to skip one ${mode.name.sentenceCase()} anyway.")
                    .formatted(Formatting.RED)
            )
            return 0
        }

        mode.associatedEntry.validateAndSet(SkipPeriod(duration, isInfinite))

        ctx.source.sendMessage(
            Text.literal("${mode.name.sentenceCase()} skipping set to: %c{${mode.associatedEntry.get()}}")
                .multiColored(listOf(mode.color), Utils.Colors.CYAN)
        )
        return 1
    }


    private fun skipForce(mode: Mode, ctx: CommandContext<ServerCommandSource>): Int {
        if (mode.opposite!!.skipForce) {
            ctx.sendCommandError("/skip${mode.name.lowercase()} force has already been set.")
            return 0
        }

        mode.skipForce = true
        performSkip(mode)

        ctx.source.sendMessage(
            Text.literal("One ${mode.name.lowercase()} has been force-skipped.")
                .formatted(Formatting.AQUA)
        )

        return 1
    }

    /**
     * Show the current status of the day/night skipping settings.
     */
    private fun showStatus(mode: Mode, ctx: CommandContext<ServerCommandSource>): Int {
        val source = ctx.source
        source.sendMessage(
            Text.literal("${mode.name.sentenceCase()}s more to skip: %c{${mode.associatedEntry.get()}}")
                .multiColored(listOf(mode.color), Utils.Colors.CYAN)
        )

        return 1
    }

    /**
     * A class that represents the number of days or nights to skip.
     *
     * @param period The number of days or nights to skip.
     * @param isInfinite Whether the period is infinite or not.
     */
    class SkipPeriod(
        @property:ValidatedInt.Restrict(min = 0, max = 100) var period: Int,
        val isInfinite: Boolean
    ) : Walkable {

        override fun toString(): String {
            return if (isInfinite) "Infinite" else period.toString()
        }

        fun isSet(): Boolean {
            return period > 0 || isInfinite
        }

        /**
         * Get the current period and decrement it by 1.
         *
         * @return The current period.
         * @throws IllegalStateException If the period is already 0.
         */
        fun getAndUpdate(): Int {
            if (isInfinite) return period

            if (period <= 0) throw IllegalStateException("Period is already 0")

            period -= 1
            return period
        }
    }
}