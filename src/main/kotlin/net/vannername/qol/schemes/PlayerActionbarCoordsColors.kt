package net.vannername.qol.schemes

import net.vannername.qol.utils.ConfigUtils.defaultPlayerConfig
import net.vannername.qol.utils.ConfigUtils.ConfigProperty.*
import net.vannername.qol.utils.Utils

class PlayerActionbarCoordsColors(
    var text: Utils.Colors = defaultPlayerConfig[ACTIONBAR_COORDS_COLOR_TEXT] as Utils.Colors,
    var coords: Utils.Colors = defaultPlayerConfig[ACTIONBAR_COORDS_COLOR_COORDS] as Utils.Colors
) {}