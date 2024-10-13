package me.vannername.qol.networking

import me.fzzyhmstrs.fzzy_config.networking.api.ClientPlayNetworkContext
import me.vannername.qol.clientutils.AFKMixinVariables

object ClientPacketReceiver {
    fun handleAFKPayload(payload: AFKPayload, context: ClientPlayNetworkContext) {
        AFKMixinVariables.setIsAFK(payload.newAFKState)
    }
}