package net.vannername.qol.commands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.vannername.qol.utils.Utils
import net.vannername.qol.utils.Utils.getPlayerData
import net.vannername.qol.utils.Utils.multiColored
import java.awt.Color

class DisplayActionbarCoords {
    init {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
            CommandManager.literal("displaycoords")
                .executes(this::run)) }

        displayCoords()
    }

    @Throws(CommandSyntaxException::class)
    private fun run(context: CommandContext<ServerCommandSource>): Int {
        val p = context.source.playerOrThrow

        val data = p.getPlayerData()

        return 1
    }

    private fun displayCoords() {
        ServerTickEvents.END_WORLD_TICK.register { world ->
            for(p in world.players) {
                p.getPlayerData().displayActionbarCoords(p)
            }
        }
    }
}