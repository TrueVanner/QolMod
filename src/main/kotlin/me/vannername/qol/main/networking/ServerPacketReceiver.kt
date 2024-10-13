package me.vannername.qol.networking

import me.fzzyhmstrs.fzzy_config.networking.api.ServerPlayNetworkContext
import me.vannername.qol.main.commands.afk.AFKUtils.stopAFK
import me.vannername.qol.main.utils.PlayerUtils.getConfig

object ServerPacketReceiver {
    fun handleAFKPayload(payload: AFKPayload, context: ServerPlayNetworkContext) {
        context.player().stopAFK()
    }
    fun handleTPCreditsPayload(payload: TPCreditsPayload, context: ServerPlayNetworkContext) {
        context.player().getConfig().tpCredits += payload.addedCredits
    }
}