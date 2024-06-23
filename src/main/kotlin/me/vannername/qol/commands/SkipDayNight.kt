package me.vannername.qol.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType.getInteger
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedAny
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt
import me.vannername.qol.QoLMod
import me.vannername.qol.QoLMod.serverConfig
import me.vannername.qol.utils.Utils
import me.vannername.qol.utils.Utils.appendCommandSuggestion
import me.vannername.qol.utils.Utils.multiColored
import me.vannername.qol.utils.Utils.sendCommandError
import me.vannername.qol.utils.Utils.sentenceCase
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.*

class SkipDayNight {

    // TODO: resolve errors with skip force
    // TODO: Add suggestions for duration
    init {
        Mode.NIGHT.opposite = Mode.DAY
        Mode.DAY.opposite = Mode.NIGHT
        register()
        detectTimeChange()
    }

    private fun register() {

        fun currentMode(ctx: CommandContext<ServerCommandSource>): Mode {
            return if (ctx.input.contains("skipday")) Mode.DAY else Mode.NIGHT
        }

        val skipDayNode = CommandManager
            .literal("skipday")
            .build()

        val skipNightNode = CommandManager
            .literal("skipnight")
            .build()

        val durationNode = CommandManager
            .argument("duration", IntegerArgumentType.integer())
//            .suggests()
            .executes { ctx ->
                skipPeriod(currentMode(ctx), getInteger(ctx, "duration"), false, ctx)
            }
            .build()

        val infNode = CommandManager
            .literal("inf")
            .executes { ctx ->
                skipPeriod(currentMode(ctx), 0, true, ctx)
            }
            .build()

        val forceNode = CommandManager
            .literal("force")
            .executes { ctx ->
                skipForce(currentMode(ctx), ctx)
                1
            }
            .build()

        val statusNode = CommandManager
            .literal("status")
            .executes { ctx ->
                showStatus(currentMode(ctx), ctx)
            }
            .build()


        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.root.addChild(skipNightNode)
            dispatcher.root.addChild(skipDayNode)

            skipNightNode.addChild(durationNode)
            skipNightNode.addChild(infNode)
            skipNightNode.addChild(forceNode)
            skipNightNode.addChild(statusNode)

            skipDayNode.addChild(durationNode)
            skipDayNode.addChild(infNode)
            skipDayNode.addChild(forceNode)
            skipDayNode.addChild(statusNode)
        }
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
                val mode = if (world.timeOfDay in 12000..23459) Mode.NIGHT else Mode.DAY
                performSkip(mode)
            } catch (e: IllegalStateException) {
                // Should only be thrown if the period is 0 and skipping is
                // impossible; Do nothing
            }
        }
    }

    @Throws(IllegalStateException::class)
    private fun performSkip(mode: Mode) {
        val world = QoLMod.defaultWorld!!
        val currentValue = mode.associatedEntry.get().getAndUpdate()

        if (!mode.opposite!!.skipForce) {
            world.timeOfDay = if (mode == Mode.DAY) 13000 else 0
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


    private fun skipForce(mode: Mode, ctx: CommandContext<ServerCommandSource>) {
        if (mode.opposite!!.skipForce) {
            ctx.sendCommandError("/skip${mode.name.lowercase()} force has already been set.")
            return
        }

        mode.skipForce = true
        performSkip(mode)

        ctx.source.sendMessage(
            Text.literal("One ${mode.name.lowercase()} has been force-skipped.")
                .formatted(Formatting.AQUA)
        )
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
    class SkipPeriod(period: Int, val isInfinite: Boolean) {
        val period = ValidatedInt(period, 100, 0)

        override fun toString(): String {
            return if (isInfinite) "Infinite" else period.get().toString()
        }

        fun isSet(): Boolean {
            return period.get() != 0 || isInfinite
        }

        /**
         * Get the current period and decrement it by 1.
         *
         * @return The current period.
         * @throws IllegalStateException If the period is already 0.
         */
        fun getAndUpdate(): Int {
            val toReturn = period.get()
            if (isInfinite) return toReturn

            if (toReturn <= 0) throw IllegalStateException("Period is already 0")

            period.validateAndSet(toReturn - 1)
            return toReturn
        }
    }
}