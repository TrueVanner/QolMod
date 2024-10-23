package me.vannername.qol.main.commands.skipdaynight

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType.getInteger
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import me.vannername.qol.main.commands.skipdaynight.SkipDayNightUtils.Mode
import me.vannername.qol.main.commands.skipdaynight.SkipDayNightUtils.SkipPeriod
import me.vannername.qol.main.commands.skipdaynight.SkipDayNightUtils.performSkip
import me.vannername.qol.main.commands.util.ServerCommandHandlerBase
import me.vannername.qol.main.utils.Utils
import me.vannername.qol.main.utils.Utils.appendCommandSuggestion
import me.vannername.qol.main.utils.Utils.multiColored
import me.vannername.qol.main.utils.Utils.sendCommandError
import me.vannername.qol.main.utils.Utils.sendWarning
import me.vannername.qol.main.utils.Utils.sentenceCase
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
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
        SkipDayNightUtils.detectTimeChange()
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
//            dispatcher.root.addChild(
//                literal<ServerCommandSource>("sd")
//                    .redirect(SkipDayNightCommandNodeKeys.ROOT_SKIPDAY.getNode())
//                    .executes(::help)
//                    .build()
//            )
//            dispatcher.root.addChild(
//                literal<ServerCommandSource>("sn")
//                    .redirect(SkipDayNightCommandNodeKeys.ROOT_SKIPNIGHT.getNode())
//                    .executes(::help)
//                    .build()
//            )

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
        if (mode.opposite!!.associatedEntry.isSet()) {

            ctx.source.sendMessage(
                Text.literal("${mode.opposite!!.name.sentenceCase()} skipping has already been set. Use ")
                    .appendCommandSuggestion("/skip${mode.name.lowercase()} force")
                    .append(" to skip one ${mode.name.lowercase()} anyway.")
                    .formatted(Formatting.RED)
            )
            return 0
        }

        mode.associatedEntry = SkipPeriod(duration, isInfinite)
        SkipDayNightUtils.forceUpdateConfig()

        ctx.source.sendMessage(
            if (duration > 0)
                Text.literal("${mode.name.sentenceCase()} skipping set to: %c{${mode.associatedEntry}}")
                    .multiColored(listOf(mode.color), Utils.Colors.CYAN)
            else
                Text.literal("${mode.name.sentenceCase()} skipping successfully disabled.")
        )
        return 1
    }


    private fun skipForce(mode: Mode, ctx: CommandContext<ServerCommandSource>): Int {
        if (mode.opposite!!.skipForce) {
            mode.opposite!!.skipForce = false
            ctx.sendWarning("Warning: /skip${mode.opposite!!.name.lowercase()} force has been set prior.")
        }

        if (SkipDayNightUtils.worldTimeToMode(ctx.source.world) != mode) {
            return ctx.sendCommandError("Can't force-skip ${mode.name.lowercase()} when it is not ${mode.name.lowercase()} bro")
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
            Text.literal("${mode.name.sentenceCase()}s more to skip: %c{${mode.associatedEntry}}")
                .multiColored(listOf(mode.color), Utils.Colors.CYAN)
        )

        return 1
    }
}