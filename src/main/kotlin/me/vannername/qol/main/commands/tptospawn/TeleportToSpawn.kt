package me.vannername.qol.main.commands.tptospawn

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import me.vannername.qol.main.commands.tptospawn.TPToSpawnUtils.TeleportData
import me.vannername.qol.main.commands.tptospawn.TPToSpawnUtils.checkTeleportRequirements
import me.vannername.qol.main.commands.tptospawn.TPToSpawnUtils.getTeleportDestination
import me.vannername.qol.main.commands.util.ServerCommandHandlerBase
import me.vannername.qol.main.utils.PlayerUtils.getConfig
import me.vannername.qol.main.utils.PlayerUtils.sendSimpleMessage
import me.vannername.qol.main.utils.Utils.Colors
import me.vannername.qol.main.utils.Utils.multiColored
import me.vannername.qol.main.utils.Utils.sendCommandError
import me.vannername.qol.main.utils.Utils.sendCommandSuccess
import me.vannername.qol.main.utils.WorldBlockPos
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object TeleportToSpawn : ServerCommandHandlerBase("home") {

    private enum class TPToSpawnCommandNodeKey : CommandNodeKey {
        QUERY;

        override fun key(): String = this.name
    }

    override fun init() {
        setDefaultAction(::run)
        super.init()
    }

    override fun registerCommandNodes() {
        super.registerCommandNodes()
        CommandManager
            .literal("query")
            .executes(::query)
            .register(TPToSpawnCommandNodeKey.QUERY)
    }

    override fun commandStructure() {
        super.commandStructure()
        ROOT.addChild(TPToSpawnCommandNodeKey.QUERY)
    }

    override fun defineHelpMessages() {
        super.defineHelpMessages()
        addPathDescriptions(
            listOf(ROOT) to "Teleports you to your or the world spawn, depending on whether you have a spawn point in the current world, " +
                    "in exchange for some TP credits. TP credits are passively generated and the generation rate is reduced " +
                    "if you aren't active. 1 TP credit allows you to travel 1000 blocks.",
            listOf(
                ROOT,
                TPToSpawnCommandNodeKey.QUERY
            ) to "Checks if you can teleport and displays the cost of teleportation " +
                    "as well as your current balance."
        )
    }

    @Throws(CommandSyntaxException::class)
    override fun run(ctx: CommandContext<ServerCommandSource>): Int {
        val p = ctx.source.playerOrThrow

        val tpData = p.checkTeleportRequirements()
        if (tpData is TeleportData.Success) {
//            p.teleportTo(TeleportTarget())
            p.requestTeleport(tpData.dest.to.x.toDouble(), tpData.dest.to.y.toDouble(), tpData.dest.to.z.toDouble())

            // a small check to make sure that the player was really teleported
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

    @Throws(CommandSyntaxException::class)
    fun query(ctx: CommandContext<ServerCommandSource>): Int {
        val p = ctx.source.playerOrThrow
        val destination = p.getTeleportDestination()
        val distance = destination.to.distanceToBlockPos(p.blockPos)
        val cost = TPToSpawnUtils.calculateTPCost(distance)
        val balance = p.getConfig().tpCredits
        val isEnough = cost <= p.getConfig().tpCredits

        p.sendMessage(
            Text.literal(
                "Distance to ${if (destination.toSpawn) "spawn" else "world's spawn (your spawn point in this world doesn't exist)"}: " +
                        "%c1{${
                            String.format(
                                "%.2f",
                                distance
                            )
                        }} blocks | %c2{${if (isEnough) "$cost (${cost.getFraction(balance)})" else cost}} TCPs\n" +
                        "Balance: %c2{${p.getConfig().tpCredits}} TPCs"
            ).multiColored(listOf(Colors.WHITE, if (isEnough) Colors.GREEN else Colors.RED), Colors.CYAN)
        )

        return 1
    }
}