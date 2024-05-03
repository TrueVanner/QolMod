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

}

