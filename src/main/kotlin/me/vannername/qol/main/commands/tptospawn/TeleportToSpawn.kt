package me.vannername.qol.main.commands.tptospawn

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import me.vannername.qol.main.commands.tptospawn.TPToSpawnUtils.TeleportData
import me.vannername.qol.main.commands.tptospawn.TPToSpawnUtils.checkTeleportRequirements
import me.vannername.qol.main.commands.util.ServerCommandHandlerBase
import me.vannername.qol.main.utils.PlayerUtils.sendSimpleMessage
import me.vannername.qol.main.utils.Utils.sendCommandError
import me.vannername.qol.main.utils.Utils.sendCommandSuccess
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.Formatting

/**
 * Template for a command handler.
 */
object TeleportToSpawn : ServerCommandHandlerBase("/home") {

    override fun defineHelpMessages() {
        super.defineHelpMessages()
        addPathDescriptions(
            // listOf(<key>) to "description",
            // ...
        )
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