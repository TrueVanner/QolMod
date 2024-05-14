package net.vannername.qol.schemes

import me.fzzyhmstrs.fzzy_config.annotations.ClientModifiable
import me.fzzyhmstrs.fzzy_config.annotations.Comment
import me.fzzyhmstrs.fzzy_config.annotations.WithPerms
import me.fzzyhmstrs.fzzy_config.config.Config
import me.fzzyhmstrs.fzzy_config.config.ConfigSection
import me.fzzyhmstrs.fzzy_config.util.Translatable
import me.fzzyhmstrs.fzzy_config.validation.minecraft.ValidatedIdentifier
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedAny
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedColor
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionOptions
import net.minecraft.world.dimension.DimensionType
import net.vannername.qol.utils.WorldBlockPos
import net.vannername.qol.utils.Utils
import org.jetbrains.annotations.PropertyKey
import java.awt.Dimension
import java.util.*

// WARNING: setting object of PlayerData with null values throws an exception!

class PlayerConfig(uuid: UUID) : Config(Utils.MyIdentifier(uuid.toString())) {
    var sendCoordinatesOfDeath = true
    var sendCoordinatesAboveHotbar = true

    @WithPerms(4)
    var isAFK = false
    @WithPerms(4)
    var isSitting = false

    var colorsOfCoordsAboveHotbar = PlayerActionbarCoordsColors()
    class PlayerActionbarCoordsColors : ConfigSection() {
        var colorOfText = ValidatedColor(Utils.Colors.YELLOW.c, false)
        var colorOfCoords = ValidatedColor(Utils.Colors.GREEN.c, false)
    }

    @WithPerms(4)
    var navData = PlayerNavigationData()
    class PlayerNavigationData : ConfigSection() {
        var isNavigating = false
        var whereTo = ValidatedAny(WorldBlockPos(0, 0, 0, World.OVERWORLD))
        var isDirect = ValidatedBoolean(false)
    }
}

