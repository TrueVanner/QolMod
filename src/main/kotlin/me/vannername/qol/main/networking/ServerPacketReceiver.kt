package me.vannername.qol.networking

import me.fzzyhmstrs.fzzy_config.networking.api.ServerPlayNetworkContext
import me.vannername.qol.main.utils.PlayerUtils.stopAFK

object ServerPacketReceiver {
    fun handleAFKPayload(payload: AFKPayload, context: ServerPlayNetworkContext) {
        context.player().stopAFK()
    }
}