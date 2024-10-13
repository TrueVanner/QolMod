package me.vannername.qol.client.utils

import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object ClientUtils {
    fun CommandContext<FabricClientCommandSource>.sendMessage(text: Text) {
        source.player.sendMessage(text, false)
    }

    fun CommandContext<FabricClientCommandSource>.sendSimpleMessage(text: String, formatting: Formatting? = null) {
        val toSend = Text.literal(text)
        if (formatting == null) toSend.formatted(formatting)
        this.sendMessage(toSend)
    }

    fun CommandContext<FabricClientCommandSource>.sendCommandSuccess(text: String): Int {
        val toSend = Text.literal(text).formatted(Formatting.GREEN)
        this.sendMessage(toSend)
        return 1
    }

    fun CommandContext<FabricClientCommandSource>.sendCommandError(text: String): Int {
        val toSend = Text.literal(text).formatted(Formatting.RED)
        source.sendError(toSend)
        return 0
    }
}