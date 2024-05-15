package net.vannername.qol.utils

import kotlinx.serialization.Serializable
import me.fzzyhmstrs.fzzy_config.annotations.ClientModifiable
import me.fzzyhmstrs.fzzy_config.annotations.WithPerms
import me.fzzyhmstrs.fzzy_config.config.Config
import me.fzzyhmstrs.fzzy_config.config.ConfigSection
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedColor
import net.minecraft.world.World
import java.util.*

// WARNING: setting object of PlayerData with null values throws an exception!
class PlayerConfig(uuid: UUID) : Config(Utils.MyIdentifier(uuid.toString()), "playerconfig") {
//    @ClientModifiable
    var sendCoordinatesOfDeath = true
//    @ClientModifiable
    var sendCoordinatesAboveHotbar = true

    var isAFK = false
    var isSitting = false

//    @ClientModifiable
    var colorsOfCoordsAboveHotbar = PlayerCoordsColors()
//    @ClientModifiable
    var colorsOfNavigationCoords = PlayerCoordsColors(text = Utils.Colors.CYAN)
    class PlayerCoordsColors(text: Utils.Colors = Utils.Colors.YELLOW, coords: Utils.Colors = Utils.Colors.GREEN) : ConfigSection() {
        var colorOfText = ValidatedColor(text.c, false)
        var colorOfCoords = ValidatedColor(coords.c, false)
    }

    var navData = PlayerNavigationData(false, WorldBlockPos(0, 0, 0, World.OVERWORLD), false)
    class PlayerNavigationData(var isNavigating: Boolean, val to: WorldBlockPos, val isDirect: Boolean) : ConfigSection() {
    }
}

