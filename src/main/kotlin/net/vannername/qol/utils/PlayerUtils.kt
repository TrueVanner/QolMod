package net.vannername.qol.utils

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.vannername.qol.QoLMod
import net.vannername.qol.utils.Utils.multiColored

object PlayerUtils {
    fun ServerPlayerEntity.displayActionbarCoords() {
        val data = QoLMod.playerConfigs[this.uuid]!!
        if(data.sendActionbarCoords.get()) {
            val pos = this.blockPos
            val colors = listOf(data.actionbarCoordsColors.text.toInt(), data.actionbarCoordsColors.coords.toInt())
            this.sendMessage(
                Text.literal("%c1{X}: %c2{${pos.x}} %c1{Y}: %c2{${pos.y}} %c1{Z}: %c2{${pos.z}}").multiColored(
                colors), true)
        }
    }

    fun ServerPlayerEntity.startAFK() {

    }
}