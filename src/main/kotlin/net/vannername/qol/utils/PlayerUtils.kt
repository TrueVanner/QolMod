package net.vannername.qol.utils

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import net.vannername.qol.QoLMod
import net.vannername.qol.utils.Utils.multiColored
import kotlin.math.abs

object PlayerUtils {
    fun PlayerEntity.displayActionbarCoords() {
        val data = getConfig()
        if(data.sendCoordinatesAboveHotbar) {
            val pos = blockPos
            val colors = listOf(data.colorsOfCoordsAboveHotbar.colorOfText.toInt(), data.colorsOfCoordsAboveHotbar.colorOfCoords.toInt())
            sendMessage(
                Text.literal("%c1{X}: %c2{${pos.x}} %c1{Y}: %c2{${pos.y}} %c1{Z}: %c2{${pos.z}}")
                    .multiColored(colors), true)
        }
    }

    fun PlayerEntity.displayNavCoords() {
        val data = getConfig()

        if(data.navData.isNavigating) {
            val from = blockPos
            val to = data.navData.to
            val colors = listOf(data.colorsOfNavigationCoords.colorOfText.toInt(), data.colorsOfNavigationCoords.colorOfCoords.toInt())
            sendMessage(
                Text.literal("%c1{X}: %c2{${abs(from.x - to.x)}} %c1{Y}: %c2{${abs(from.y - to.y)}} %c1{Z}: %c2{${abs(from.z - to.z)}}")
                    .multiColored(colors), true)
        }
    }

    @JvmStatic
    fun PlayerEntity.hasConfig(): Boolean {
        return QoLMod.playerConfigs.containsKey(uuid)
    }

    @JvmStatic
    fun PlayerEntity.getConfig(): PlayerConfig {
        if(!hasConfig()) throw NullPointerException("Tried to get config that wasn't loaded")
        return QoLMod.playerConfigs[uuid]!!
    }

    fun PlayerEntity.startAFK() {

    }
}