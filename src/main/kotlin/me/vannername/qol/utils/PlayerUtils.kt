package me.vannername.qol.utils

import me.vannername.qol.GlobalMixinVariables
import me.vannername.qol.QoLMod
import me.vannername.qol.config.PlayerConfig
import me.vannername.qol.utils.Utils.multiColored
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import kotlin.math.abs

object PlayerUtils {
    /**
     * Displays player's current coordinates above the hotbar.
     */
    fun PlayerEntity.displayActionbarCoords() {
        val data = getConfig()
        if (data.sendCoordinatesAboveHotbar) {
            val pos = blockPos
            val colors = listOf(
                data.colorsOfCoordsAboveHotbar.colorOfText.toInt(),
                data.colorsOfCoordsAboveHotbar.colorOfCoords.toInt()
            )
            sendMessage(
                Text.literal("%c1{X}: %c2{${pos.x}} %c1{Y}: %c2{${pos.y}} %c1{Z}: %c2{${pos.z}}")
                    .multiColored(colors), true
            )
        }
    }

    /**
     * Displays the coordinates of the player's navigation target above the hotbar.
     * Overwrites the current coordinates if they are being displayed.
     */
    fun PlayerEntity.displayNavCoords() {
        val data = getConfig()

        if (data.navData.isNavigating) {
            val from = blockPos
            val to = data.navData.target.get()
            val colors = listOf(
                data.colorsOfNavigationCoords.colorOfText.toInt(),
                data.colorsOfNavigationCoords.colorOfCoords.toInt()
            )
            sendMessage(
                Text.literal(
                    "%c1{X}: %c2{${abs(from.x - to.x)}} %c1{Y}: %c2{${abs(from.y - to.y)}} %c1{Z}: %c2{${
                        abs(
                            from.z - to.z
                        )
                    }}"
                ).multiColored(colors), true
            )
        }
    }

    /**
     * Checks if the player has a configuration object.
     */
    @JvmStatic
    fun PlayerEntity.hasConfig(): Boolean {
        return QoLMod.playerConfigs.containsKey(uuid)
    }

    /**
     * Obtains the player's configuration object.
     */
    @JvmStatic
    fun PlayerEntity.getConfig(): PlayerConfig {
        if (!hasConfig()) throw NullPointerException("Tried to get config that wasn't loaded")
        return QoLMod.playerConfigs[uuid]!!
    }

    /**
     * Simplifies sending a simple text message with basic formatting.
     */
    fun PlayerEntity.simpleMessage(text: String, formatting: Formatting? = null, overlay: Boolean = false) {
        val toSend = Text.literal(text)
        if (formatting != null) toSend.formatted(formatting)
        this.sendMessage(toSend, overlay)
    }

    fun PlayerEntity.startAFK() {
        this.getConfig().isAFK = true
        this.isInvulnerable = true
        GlobalMixinVariables.setPlayerIsInvulnerable(true)
        this.simpleMessage("You have entered AFK mode.", Formatting.RED)
    }

    fun PlayerEntity.stopAFK() {
        this.getConfig().isAFK = false
        Thread {
            Thread.sleep(3 * 1000)
            this.isInvulnerable = false
            GlobalMixinVariables.setPlayerIsInvulnerable(false)
        }.start()
        this.simpleMessage("You are no longer AFK!", Formatting.GREEN)
    }

    fun PlayerEntity.maintainAFK() {

    }

    /**
     * Starts navigation. This implies that:
     * - For the duration of the navigation, the player will receive the coordinates of the destination above the hotbar
     * - If the player has the mod installed client-side, a compass will be rendered next to their hotbar pointing
     * to the destination.
     *
     * @param to The coordinates to navigate to.
     * @param isDirect if true, navigation will stop only when the coordinate is reached.
     * Otherwise, navigation is stopped if the player is within 3 blocks of the target.
     */
    fun PlayerEntity.startNavigation(to: WorldBlockPos, isDirect: Boolean) {
        this.getConfig().navData.isNavigating = true
        this.getConfig().navData =
            PlayerConfig.PlayerNavigationData(true, to, isDirect)
    }

    fun PlayerEntity.stopNavigation() {
        this.getConfig().navData.isNavigating = false
    }
}