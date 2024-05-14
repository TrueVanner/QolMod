package net.vannername.qol.utils

import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedAny
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedString
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.vannername.qol.QoLMod
import net.vannername.qol.schemes.PlayerConfig
import net.vannername.qol.utils.Utils.multiColored

object PlayerUtils {
    fun PlayerEntity.displayActionbarCoords() {
        val data = this.getConfig()
        if(data.sendCoordinatesOfDeath) {
            val pos = this.blockPos
            val colors = listOf(data.colorsOfCoordsAboveHotbar.colorOfText.toInt(), data.colorsOfCoordsAboveHotbar.colorOfCoords.toInt())
            this.sendMessage(
                Text.literal("%c1{X}: %c2{${pos.x}} %c1{Y}: %c2{${pos.y}} %c1{Z}: %c2{${pos.z}}")
                    .multiColored(colors), true)
        }
    }

    fun PlayerEntity.getConfig(): PlayerConfig {
        return QoLMod.playerConfigs[this.uuid]!!
    }

    fun PlayerEntity.startAFK() {

    }
}