package me.vannername.qol.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType.getInteger
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt
import me.vannername.qol.QoLMod.serverConfig
import me.vannername.qol.utils.Utils
import me.vannername.qol.utils.Utils.multiColored
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.*

class SkipDayNight {

    init {
        register()
        detectTimeChange()
    }

    private var skipNightForce = false
    private var skipDayForce = false

    private fun register() {
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
                println(ctx.rootNode.name)
                println(ctx.command.toString())
                skipPeriod(Mode.NIGHT, getInteger(ctx, "duration"), false, ctx)
            }
            .build()

        val infNode = CommandManager
            .literal("inf")
            .executes { ctx ->
                skipPeriod(Mode.NIGHT, 1, true, ctx)
            }
            .build()

        val forceNode = CommandManager
            .literal("force")
            .executes { _ ->
                skipNightForce = true
                1
            }
            .build()

        val statusNode = CommandManager
            .literal("status")
            .executes(::showStatus)
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
            // shortcuts
            val days = serverConfig.skippingSettings.daysToSkip.get()
            val nights = serverConfig.skippingSettings.nightsToSkip.get()

            try {
                if (world.timeOfDay in 12000..23459) {
                    // Night
                    skipNightForce = false
                    if (days.getAndUpdate() > 0 && !skipDayForce) {
                        world.timeOfDay = 0
                        world.players.forEach {
                            it.sendMessage(Text.literal("Night successfully skipped!").formatted(Formatting.AQUA))
                        }
                    }
                } else {
                    // Day
                    skipDayForce = false
                    if (nights.getAndUpdate() > 0 && !skipNightForce) {
                        world.timeOfDay = 13000
                        world.players.forEach {
                            it.sendMessage(Text.literal("Day successfully skipped!").formatted(Formatting.AQUA))
                        }
                    }
                }
            } catch (e: IllegalStateException) {
                // Should only be thrown if the period is 0 and skipping is
                // impossible; Do nothing
            }
        }
    }

    private enum class Mode {
        DAY,
        NIGHT
    }

    @Throws(CommandSyntaxException::class)
    private fun skipPeriod(
        mode: Mode,
        duration: Int,
        isInfinite: Boolean,
        context: CommandContext<ServerCommandSource>
    ): Int {

        if (mode == Mode.DAY) {
            serverConfig.skippingSettings.daysToSkip.validateAndSet(SkipPeriod(duration, isInfinite))
        } else {
            serverConfig.skippingSettings.nightsToSkip.validateAndSet(SkipPeriod(duration, isInfinite))
        }
        return 1
    }

    /**
     * Show the current status of the day/night skipping settings.
     */
    private fun showStatus(context: CommandContext<ServerCommandSource>): Int {
        val source = context.source
        source.sendMessage(
            Text.literal("Nights more to skip: %c{${serverConfig.skippingSettings.nightsToSkip.get()}}")
                .multiColored(listOf(Utils.Colors.GRAY), Utils.Colors.CYAN)
        )
        source.sendMessage(
            Text.literal("Days more to skip: %c{${serverConfig.skippingSettings.daysToSkip.get()}}")
                .multiColored(listOf(Utils.Colors.YELLOW), Utils.Colors.CYAN)
        )

        return 1
    }

    /**
     * A class that represents the number of days or nights to skip.
     *
     * @param period The number of days or nights to skip.
     * @param isInfinite Whether the period is infinite or not.
     */
    class SkipPeriod(period: Int, private val isInfinite: Boolean) {
        private val period = ValidatedInt(period, 100, 0)

        override fun toString(): String {
            return if (isInfinite) "Infinite" else period.toString()
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