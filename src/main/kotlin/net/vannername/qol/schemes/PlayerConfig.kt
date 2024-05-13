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
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionOptions
import net.minecraft.world.dimension.DimensionType
import net.vannername.qol.utils.WorldBlockPos
import net.vannername.qol.utils.Utils
import java.awt.Dimension
import java.util.*

// WARNING: setting object of PlayerData with null values throws an exception!

class PlayerConfig(uuid: UUID) : Config(Utils.MyIdentifier(uuid.toString())) {

    @Comment("Send coordinates of death?")
    val sendDeathCoords = ValidatedBoolean(true)
    @Comment("Display coordinates above hotbar?")
    var sendActionbarCoords = ValidatedBoolean(true)
    @WithPerms(4)
    var isAFK = ValidatedBoolean(false)
    @WithPerms(4)
    var isSitting = ValidatedBoolean(false)

    var actionbarCoordsColors = PlayerActionbarCoordsColors()
    class PlayerActionbarCoordsColors : ConfigSection() {
        var text = ValidatedColor(Utils.Colors.YELLOW.c, false)
        var coords = ValidatedColor(Utils.Colors.GREEN.c, false)
    }

    @Comment("Navigation data")
    var navData = PlayerNavigationData()
    class PlayerNavigationData : ConfigSection() {
        @WithPerms(4)
        var isNavigating = ValidatedBoolean(false)
        @Comment("Where to?")
        var worldBlockPos = ValidatedAny(WorldBlockPos(0, 0, 0, World.OVERWORLD.value))
        @Comment("Is direct?")
        var isDirect = ValidatedBoolean(false)
    }
}

