package net.vannername.qol.utils

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.vannername.qol.schemes.PlayerData
import net.vannername.qol.utils.Utils.getPlayerData
import net.vannername.qol.utils.Utils.multiColored

object PlayerUtils {
    fun ServerPlayerEntity.displayActionbarCoords() {
        val data = this.getPlayerData()
        if(data.sendActionbarCoords) {
            val intLoc = Utils.getIntegerLocation(this)

            this.sendMessage(
                Text.literal("%c1{X}: %c2{${intLoc[0]}} %c1{Y}: %c2{${intLoc[1]}} %c1{Z}: %c2{${intLoc[2]}}").multiColored(
                listOf(data.actionbarCoordsColors.text, data.actionbarCoordsColors.coords)), true)
        }
    }
}