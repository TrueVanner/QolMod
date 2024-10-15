package me.vannername.qol.main.commands.tptospawn

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import me.vannername.qol.main.commands.tptospawn.TPToSpawnUtils.TeleportData
import me.vannername.qol.main.commands.tptospawn.TPToSpawnUtils.checkTeleportRequirements
import me.vannername.qol.main.commands.util.ServerCommandHandlerBase
import me.vannername.qol.main.utils.PlayerUtils.getConfig
import me.vannername.qol.main.utils.PlayerUtils.sendSimpleMessage
import me.vannername.qol.main.utils.Utils.sendCommandError
import me.vannername.qol.main.utils.Utils.sendCommandSuccess
import me.vannername.qol.main.utils.WorldBlockPos
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.Formatting

object TeleportToSpawn : ServerCommandHandlerBase("home") {

    override fun init() {
        setDefaultAction(::run)
        super.init()
    }

    @Throws(CommandSyntaxException::class)
    override fun run(ctx: CommandContext<ServerCommandSource>): Int {
        val p = ctx.source.playerOrThrow

        val tpData = p.checkTeleportRequirements()
        if (tpData is TeleportData.Success) {
            p.requestTeleport(tpData.to.x.toDouble(), tpData.to.y.toDouble(), tpData.to.z.toDouble())
            val oldPos = WorldBlockPos.ofPlayer(p)
            Thread {
                Thread.sleep(50)
                if (!oldPos.isWithinDistance(WorldBlockPos.ofPlayer(p), 10.0)) {
                    p.getConfig().tpCredits -= tpData.cost
                } else {
                    p.sendSimpleMessage("Something went wrong. Your TPCs were not deducted.", Formatting.RED)
                }
            }
            return ctx.sendCommandSuccess("You're back!")
        } else {
            return ctx.sendCommandError((tpData as TeleportData.Error).errorMessage)
        }
    }
}