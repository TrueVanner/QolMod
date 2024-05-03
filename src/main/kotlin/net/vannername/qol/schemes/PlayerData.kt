package net.vannername.qol.schemes

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.vannername.qol.utils.ConfigUtils.defaultPlayerConfig
import net.vannername.qol.utils.ConfigUtils.ConfigProperties.*
import net.vannername.qol.utils.Utils
import net.vannername.qol.utils.Utils.multiColored

// WARNING: setting object of PlayerData with null values throws an exception!

class PlayerData(
    var sendDeathCoords: Boolean = defaultPlayerConfig[SEND_DEATH_COORDS]  as Boolean,
    var sendActionbarCoords: Boolean = defaultPlayerConfig[SEND_ACTIONBAR_COORDS] as Boolean,
    val navData: PlayerNavigationData = PlayerNavigationData(),
    val actionbarCoordsColors: PlayerActionbarCoordsColors = PlayerActionbarCoordsColors(),
) {

    fun displayActionbarCoords(p: ServerPlayerEntity) {
        if(sendActionbarCoords) {
            val intLoc = Utils.getIntegerLocation(p)

            p.sendMessage(Text.literal("%c1{X}: %c2{${intLoc[0]}} %c1{Y}: %c2{${intLoc[1]}} %c1{Z}: %c2{${intLoc[2]}}").multiColored(
                listOf(actionbarCoordsColors.text, actionbarCoordsColors.coords)), true)
        }
    }
}

