package me.vannername.qol.networking

import me.fzzyhmstrs.fzzy_config.networking.api.ClientPlayNetworkContext
import me.vannername.qol.clientutils.AFKMixinVariables
import me.vannername.qol.main.networking.payloads.AFKPayload

object ClientPacketReceiver {
    fun handleAFKPayload(payload: AFKPayload, context: ClientPlayNetworkContext) {
        AFKMixinVariables.setIsAFK(payload.newAFKState)
    }
}