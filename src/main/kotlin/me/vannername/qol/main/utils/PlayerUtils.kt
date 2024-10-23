package me.vannername.qol.main.utils

import me.vannername.qol.main.QoLMod
import me.vannername.qol.main.config.PlayerConfig
import me.vannername.qol.main.utils.Utils.multiColored
import net.minecraft.entity.decoration.ItemFrameEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.text.MutableText
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
    fun PlayerEntity.sendSimpleMessage(message: Any, formatting: Formatting? = null, overlay: Boolean = false) {
        val toSend = Text.literal(message.toString())
        if (formatting != null) toSend.formatted(formatting)
        sendMessage(toSend, overlay)
    }

    /**
     * Simplifies sending a debug message with yellow formatting.
     */
    fun PlayerEntity.sendDebugMessage(message: Any) {
        val toSend = Text.literal("[QoLMod] ${message}")
            .formatted(Formatting.YELLOW)
        sendMessage(toSend, false)
    }

    fun PlayerEntity.getCoordsString(): String {
        return "${blockPos.x} ${blockPos.y} ${blockPos.z}"
    }

    fun PlayerEntity.lightUpNearestInvisibleItemFrames() {
        // each frame that is invisible is added to the list if the player
        // is holding an amethyst shard in their offhand. Otherwise,
        // the list is empty - thanks to this, the next function immediately
        // removes the glowing effects from the items when the shard
        // is removed from the offhand. The second function also runs on a
        // slightly larger radius to ensure that item frames out of the effective range
        // (defined by me as 3 blocks) are not affected.
        // the first check checks for both invisibility and glowing
        // as otherwise item frames never stay in "close" for more than one tick.
        val close = if (offHandStack.isOf(Items.AMETHYST_SHARD))
            world.getEntitiesByClass<ItemFrameEntity>(
                ItemFrameEntity::class.java,
                boundingBox.expand(3.0),
                { it.isInvisible || it.isGlowing }
            ) else listOf()
        close.forEach {
            it.isInvisible = false
            it.isGlowing = true
        }
        world.getEntitiesByClass<ItemFrameEntity>(
            ItemFrameEntity::class.java,
            boundingBox.expand(4.0),
            { it.isGlowing && !close.contains(it) }
        ).forEach {
            it.isGlowing = false
            it.isInvisible = true
        }
    }

    fun PlayerEntity.coloredName(): MutableText {
        return Text.literal(name.string).setStyle(styledDisplayName.style)
    }
}