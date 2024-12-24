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
import me.vannername.qol.main.utils.Utils.sendMessage
import me.vannername.qol.main.utils.Utils.sendSimpleMessage
import me.vannername.qol.main.utils.Utils.sendWarning
import me.vannername.qol.main.utils.Utils.sentenceCase
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandSource
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

// as register() is fully overwritten, command name is purely decorative
object SkipDayNight : ServerCommandHandlerBase("skipdaynight") {

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
        STOP,
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
                skipPeriod(currentMode(ctx), getInteger(ctx, "duration"), ctx)
            }
            .register(SkipDayNightCommandNodeKeys.DURATION)

        CommandManager
            .literal("inf")
            .executes { ctx ->
                skipPeriod(currentMode(ctx), -1, ctx)
            }
            .register(SkipDayNightCommandNodeKeys.INF)

        CommandManager
            .literal("stop")
            .executes { ctx ->
                skipPeriod(currentMode(ctx), 0, ctx)
            }
            .register(SkipDayNightCommandNodeKeys.STOP)

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
                SkipDayNightCommandNodeKeys.STOP,
                SkipDayNightCommandNodeKeys.INF,
                SkipDayNightCommandNodeKeys.FORCE,
                SkipDayNightCommandNodeKeys.STATUS
            )

        SkipDayNightCommandNodeKeys.ROOT_SKIPNIGHT
            .addChildren(
                HELP,
                SkipDayNightCommandNodeKeys.DURATION,
                SkipDayNightCommandNodeKeys.STOP,
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
        // intentionally kept blank
    }

    override fun help(ctx: CommandContext<ServerCommandSource>): Int {
        // simple shortcut
        fun format(param: String, descr: String): MutableText {
            return Text.literal("")
                .appendCommandSuggestion("/skipnight $param", "/skip(night|day) $param")
                .append(" - $descr").formatted(Formatting.YELLOW)
        }

        ctx.sendSimpleMessage("Help message for both /skipnight and /skipday", Formatting.YELLOW)

        ctx.sendMessage(format("[help]", "Displays this help message."))

        ctx.sendMessage(
            format(
                "<duration>",
                "Skips the specified number of days/nights. If 0 is passed, disables skipping until new activated again. If night skipping is enabled, it's not possible to set up day skipping at the same time, and vice versa: check out "
            ).appendCommandSuggestion("/skipnight force", "/skip(night|day) force").append(" (also described below).")
        )
        ctx.sendMessage(format("stop", "Same as /skip(night|day) 0."))
        ctx.sendMessage(format("inf", "Skips nights/days indefinitely until disabled."))
        ctx.sendMessage(
            format(
                "force",
                "Allows to skip one night/day even if skipping days/nights has been enabled. For example, if night skipping is enabled, /skipday force can be used to skip to the night (which will not be skipped for this time) and see your build at night. The next night will be skipped as usual."
            )
        )
        ctx.sendMessage(format("status", "Shows the number of nights/days to be skipped."))

        return 1
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
        ctx: CommandContext<ServerCommandSource>
    ): Int {
        val isInfinite = duration == -1

        if (mode.opposite!!.associatedEntry.isSet()) {

            ctx.source.sendMessage(
                Text.literal("${mode.opposite!!.name.sentenceCase()} skipping has already been set. Use ")
                    .appendCommandSuggestion("/skip${mode.name.lowercase()} force")
                    .append(" to skip one ${mode.name.lowercase()} anyway.")
                    .formatted(Formatting.RED)
            )
            return 0
        }
        // updated: now preserve duration if mode is set to infinite instead of setting it to 0
        if (!isInfinite) {
            mode.associatedEntry = SkipPeriod(duration, false)
        } else {
            mode.associatedEntry = SkipPeriod(mode.associatedEntry.period, true)
        }
        SkipDayNightUtils.forceUpdateConfig()

        ctx.source.sendMessage(
            if (duration > 0 || isInfinite)
                Text.literal("${mode.name.sentenceCase()} skipping set to: %c{${mode.associatedEntry}}")
                    .multiColored(listOf(mode.color), Utils.Colors.CYAN)
            else
                Text.literal("${mode.name.sentenceCase()} skipping successfully disabled.").formatted(Formatting.GREEN)
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