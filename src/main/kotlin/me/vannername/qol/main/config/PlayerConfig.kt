package me.vannername.qol.main.config

import me.fzzyhmstrs.fzzy_config.annotations.ClientModifiable
import me.fzzyhmstrs.fzzy_config.config.Config
import me.fzzyhmstrs.fzzy_config.config.ConfigSection
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedColor
import me.vannername.qol.main.utils.Utils
import me.vannername.qol.main.utils.WorldBlockPos
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
        target: WorldBlockPos = WorldBlockPos(),
        isDirect: Boolean = false,
        reached: Boolean = true
    ) : ConfigSection() {

        // needed to keep values modifiable
        @ClientModifiable
        var isNavigating = isNavigating

        var target = WorldBlockPos.Validated(target)

        //        var test = ValidatedMap(mapOf(listOf(0,0,0) to "minecraft:overworld"), ValidatedList(listOf(0,0,0), ValidatedInt()), ValidatedString())
//        var test = (mapOf(listOf(0,0,0) to "test"), ValidatedList(listOf(0,0,0), ValidatedInt()), ValidatedString())
        @ClientModifiable
        var isDirect = isDirect
        var reached = reached
    }

    var tpCredits: Double = 0.0
}

