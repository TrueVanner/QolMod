package me.vannername.qol.main.commands.afk

import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.vannername.qol.main.utils.PlayerUtils.getConfig
import me.vannername.qol.main.utils.PlayerUtils.sendSimpleMessage
import me.vannername.qol.networking.AFKPayload
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Formatting

object AFKUtils {
    fun ServerPlayerEntity.startAFK() {
        getConfig().isAFK = true
        ConfigApi.network().send(AFKPayload(true), this)
        isInvulnerable = true
        sendSimpleMessage("You have entered AFK mode.", Formatting.RED)
    }

    fun ServerPlayerEntity.stopAFK() {
        getConfig().isAFK = false
        Thread {
            Thread.sleep(3 * 1000)
            isInvulnerable = false
        }.start()
        sendSimpleMessage("You are no longer AFK!", Formatting.GREEN)
    }
}