package me.vannername.qol.main.config

import me.fzzyhmstrs.fzzy_config.annotations.ClientModifiable
import me.fzzyhmstrs.fzzy_config.config.Config
import me.fzzyhmstrs.fzzy_config.config.ConfigSection
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedAny
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedColor
import me.vannername.qol.main.utils.Utils
import me.vannername.qol.main.utils.WorldBlockPos
import net.minecraft.world.World
import java.util.*

// WARNING: setting object of PlayerData with null values throws an exception!
class PlayerConfig(uuid: UUID) : Config(Utils.MyIdentifier(uuid.toString()), "playerconfig") {

    /** Determines if the player should be sent coordinates of their death after dying. */
    @ClientModifiable
    var sendCoordinatesOfDeath = true

    /** Determines if their current coordinates should be sent to player's hotbar. */
    @ClientModifiable
    var sendCoordinatesAboveHotbar = true

    var isAFK = false
    var isSitting = false

    @ClientModifiable
    var colorsOfCoordsAboveHotbar = PlayerCoordsColors()

    @ClientModifiable
    var colorsOfNavigationCoords = PlayerCoordsColors(text = Utils.Colors.CYAN)

    class PlayerCoordsColors(
        text: Utils.Colors = Utils.Colors.YELLOW,
        coords: Utils.Colors = Utils.Colors.GREEN
    ) : ConfigSection() {

        @ClientModifiable
        var colorOfText = ValidatedColor(text.c, false)

        @ClientModifiable
        var colorOfCoords = ValidatedColor(coords.c, false)
    }

    @ClientModifiable
    var navData = PlayerNavigationData()

    class PlayerNavigationData(
        isNavigating: Boolean = false,
        target: WorldBlockPos = WorldBlockPos(0, 0, 0, World.OVERWORLD),
        isDirect: Boolean = false,
        reached: Boolean = true
    ) : ConfigSection() {

        // needed to keep values modifiable
        @ClientModifiable
        var isNavigating = isNavigating

        @ClientModifiable
        var target = ValidatedAny(target)

        @ClientModifiable
        var isDirect = isDirect
        var reached = reached
    }

    var tpCredits: Double = 0.0
}

