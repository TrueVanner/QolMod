package net.vannername.qol.schemes

import net.vannername.qol.utils.ConfigUtils
import net.vannername.qol.utils.ConfigUtils.defaultPlayerConfig
import net.vannername.qol.utils.ConfigUtils.ConfigProperties.*
import java.awt.Color

class PlayerActionbarCoordsColors(
    val text: Int = defaultPlayerConfig[ACTIONBAR_COORDS_COLOR_TEXT] as Int,
    val coords: Int = defaultPlayerConfig[ACTIONBAR_COORDS_COLOR_COORDS] as Int
) {}