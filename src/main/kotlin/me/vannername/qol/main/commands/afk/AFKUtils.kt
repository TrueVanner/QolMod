package me.vannername.qol.main.commands.afk

import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.vannername.qol.main.networking.payloads.AFKPayload
import me.vannername.qol.main.utils.PlayerUtils.getConfig
import me.vannername.qol.main.utils.PlayerUtils.sendSimpleMessage
import me.vannername.qol.main.utils.Utils
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object AFKUtils {
    fun ServerPlayerEntity.startAFK() {
        getConfig().isAFK = true
        Utils.saveConfig(this)
        ConfigApi.network().send(AFKPayload(true), this)
        isGlowing = true
        isInvulnerable = true
        Utils.broadcast(
            Text.literal(name.string).setStyle(styledDisplayName.style).append(" has gone AFK!")
        )//.multiColored(Formatting.RED))
        sendSimpleMessage("You have entered AFK mode.", Formatting.RED)
    }

    fun ServerPlayerEntity.stopAFK() {
        getConfig().isAFK = false
        Utils.saveConfig(this)
        isGlowing = false
        Thread {
            Thread.sleep(3 * 1000)
            isInvulnerable = false
        }.start()
        Utils.broadcast(
            Text.literal(name.string).setStyle(styledDisplayName.style).append(" has left AFK!")
        )//.multiColored(Formatting.RED))
        sendSimpleMessage("You are no longer AFK!", Formatting.GREEN)
    }
}