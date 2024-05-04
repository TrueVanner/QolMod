package net.vannername.qol.schemes

import net.vannername.qol.utils.ConfigUtils.defaultPlayerConfig
import net.vannername.qol.utils.ConfigUtils.ConfigProperty.*

// WARNING: setting object of PlayerData with null values throws an exception!

class PlayerData(
    var sendDeathCoords: Boolean = defaultPlayerConfig[SEND_DEATH_COORDS]  as Boolean,
    var sendActionbarCoords: Boolean = defaultPlayerConfig[SEND_ACTIONBAR_COORDS] as Boolean,
    var isAFK: Boolean = defaultPlayerConfig[IS_AFK] as Boolean,
    var isSitting: Boolean = defaultPlayerConfig[IS_SITTING] as Boolean,
    var navData: PlayerNavigationData = PlayerNavigationData(),
    var actionbarCoordsColors: PlayerActionbarCoordsColors = PlayerActionbarCoordsColors(),
) {

}

