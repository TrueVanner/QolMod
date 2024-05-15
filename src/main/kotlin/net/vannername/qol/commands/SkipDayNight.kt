package net.vannername.qol.commands

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.BoolArgumentType.getBool
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType.getInteger
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.command.argument.BlockPosArgumentType
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LodestoneTrackerComponent
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.GlobalPos
import net.vannername.qol.QoLMod.serverConfig
import net.vannername.qol.utils.PlayerUtils.displayActionbarCoords
import net.vannername.qol.utils.PlayerUtils.displayNavCoords
import net.vannername.qol.utils.Utils
import net.vannername.qol.utils.Utils.multiColored
import org.assertj.core.error.ShouldBeInfinite
import java.util.*
import kotlin.contracts.contract

class SkipDayNight {

    var skipNightForce = false
    var skipDayForce = false

    fun register() {
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

    fun detectTimeChange() {
        ServerTickEvents.END_WORLD_TICK.register { world ->
            // 12000 (10:00) Beginning of the Minecraft sunset.
            // Villagers go to their beds and sleep.
            // 23460 (19:33) In clear weather, beds can no longer be used.
            // In clear weather, bees leave the nest/hive.
            // In clear weather, undead mobs begin to burn.

            // TODO: finish according to MainPlugin
            if(world.timeOfDay in 12000..23459) {

            } else {

            }
        }
    }

    private enum class Mode {
        DAY,
        NIGHT
    }

    @Throws(CommandSyntaxException::class)
    private fun skipPeriod(mode: Mode, duration: Int, isInfinite: Boolean, context: CommandContext<ServerCommandSource>): Int {
        val p = context.source.playerOrThrow

        if (mode == Mode.DAY) {
            serverConfig.skippingSettings.daysToSkip.validateAndSet(SkipPeriod(duration, isInfinite))
        } else {
            serverConfig.skippingSettings.nightsToSkip.validateAndSet(SkipPeriod(duration, isInfinite))
        }
        return 1
    }

    private fun showStatus(context: CommandContext<ServerCommandSource>): Int {
        val source = context.source
        source.sendMessage(Text.literal("Nights more to skip: %c{${serverConfig.skippingSettings.nightsToSkip}}")
            .multiColored(listOf(Utils.Colors.GRAY), Utils.Colors.CYAN))
        source.sendMessage(Text.literal("Days more to skip: %c{${serverConfig.skippingSettings.daysToSkip}}")
            .multiColored(listOf(Utils.Colors.YELLOW), Utils.Colors.CYAN))

        return 1
    }

    class SkipPeriod(period: Int, val isInfinite: Boolean) {
        val period = ValidatedInt(period, 100, 0)

        override fun toString(): String {
            return if(isInfinite) "Infinite" else period.toString()
        }

        fun getAndUpdate(): Int? {
            val toReturn = period.get()
            if(isInfinite) return toReturn
            if(toReturn <= 0) return null
            period.validateAndSet(toReturn - 1)
            return toReturn - 1
        }
    }
}