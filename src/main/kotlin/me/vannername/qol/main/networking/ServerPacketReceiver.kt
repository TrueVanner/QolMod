package me.vannername.qol.main.networking

import me.fzzyhmstrs.fzzy_config.networking.api.ServerPlayNetworkContext
import me.vannername.qol.main.commands.afk.AFKUtils.stopAFK
import me.vannername.qol.main.networking.payloads.AFKPayload
import me.vannername.qol.main.networking.payloads.TPCreditsPayload
import me.vannername.qol.main.utils.PlayerUtils.getConfig
import me.vannername.qol.main.utils.Utils

object ServerPacketReceiver {
    fun handleAFKPayload(payload: AFKPayload, context: ServerPlayNetworkContext) {
        context.player().stopAFK()
    }
    fun handleTPCreditsPayload(payload: TPCreditsPayload, context: ServerPlayNetworkContext) {
        context.player().getConfig().tpCredits += payload.addedCredits.value
        Utils.saveConfig(context.player())
    }
}