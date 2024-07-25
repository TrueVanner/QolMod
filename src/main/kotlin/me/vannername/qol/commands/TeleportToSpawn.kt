package me.vannername.qol.commands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import me.vannername.qol.commands.util.CommandHandlerBase
import me.vannername.qol.utils.PlayerUtils.checkTeleportRequirements
import me.vannername.qol.utils.PlayerUtils.sendSimpleMessage
import me.vannername.qol.utils.Utils.sendCommandError
import me.vannername.qol.utils.Utils.sendCommandSuccess
import me.vannername.qol.utils.WorldBlockPos
import net.fabricmc.api.EnvType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.Formatting

/**
 * Template for a command handler.
 */
object TeleportToSpawn : CommandHandlerBase<ServerCommandSource>("/home", EnvType.SERVER) {

    override fun defineHelpMessages() {
        super.defineHelpMessages()
        addPathDescriptions(
            // listOf(<key>) to "description",
            // ...
        )
    }

    sealed class TeleportData {
        data class Success(val to: WorldBlockPos, val cost: Int, val message: String?) : TeleportData()
        data class Error(val errorMessage: String) : TeleportData()
    }

    @Throws(CommandSyntaxException::class)
    override fun run(ctx: CommandContext<ServerCommandSource>): Int {
        val p = ctx.source.playerOrThrow

        val tpData = p.checkTeleportRequirements()
        if (tpData is TeleportData.Success) {
            if (tpData.message != null) {
                p.sendSimpleMessage(tpData.message, Formatting.YELLOW)
            }
            p.addExperience(-tpData.cost)
            p.teleport(tpData.to.x.toDouble(), tpData.to.y.toDouble(), tpData.to.z.toDouble(), true)
            return ctx.sendCommandSuccess("You're back!")
        } else {
            return ctx.sendCommandError((tpData as TeleportData.Error).errorMessage)
        }
    }
}