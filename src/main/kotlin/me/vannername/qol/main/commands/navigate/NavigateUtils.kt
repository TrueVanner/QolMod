package me.vannername.qol.main.commands.navigate

import com.google.common.base.Predicate
import me.vannername.qol.main.config.PlayerConfig
import me.vannername.qol.main.utils.PlayerUtils.getConfig
import me.vannername.qol.main.utils.WorldBlockPos
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LodestoneTrackerComponent
import net.minecraft.component.type.NbtComponent
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.GlobalPos
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.Optional

object NavigateUtils {
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
    fun ServerPlayerEntity.startNavigation(to: WorldBlockPos, isDirect: Boolean) {
        getConfig().navData.isNavigating = true
        getConfig().navData =
            PlayerConfig.PlayerNavigationData(true, to, isDirect)
        giveCompass(to)
    }

    fun ServerPlayerEntity.stopNavigation() {
        getConfig().navData.isNavigating = false
        removeCompass()
    }

    /**
     * Only works if the server has the [Coord Finder](https://modrinth.com/mod/coord-finder/version/fabric-1.20.6-1.1.0) mod installed.
     * Transforms the positions specified in the config file of the mod to the list of positions in the current mod's format.
     *
     * @return the list of the positions on the server stored in positions.properties
     */
    fun decomposeCoordFinderLocations(): Map<String, WorldBlockPos> {
        try {
            val fileContents = Files.readAllLines(Path.of("config/coordfinder/places.properties"))
            return fileContents.map { line ->
                // format: name=worldID,x,y,z
                val split = line.split("=")
                val name = split[0]
                val coords = split[1].split(",")
                name to WorldBlockPos(coords[1].toInt(), coords[2].toInt(), coords[3].toInt(), coords[0])
            }.toMap()
        } catch (_: IOException) {
            // if the file doesn't exist
            return emptyMap()
        }
    }

    fun ServerPlayerEntity.giveCompass(dest: WorldBlockPos) {
        inventory.offerOrDrop(createNavigationCompass(dest))
    }

    fun ServerPlayerEntity.removeCompass() {
        Inventories.remove(
            inventory,
            Predicate<ItemStack> { item ->
                item.get<NbtComponent>(DataComponentTypes.CUSTOM_DATA)?.contains("qol_nav_compass") == true
            },
            64, false
        )
    }

    fun createNavigationCompass(dest: WorldBlockPos): ItemStack {
        val compass = ItemStack(Items.COMPASS)
        compass.set(
            DataComponentTypes.LODESTONE_TRACKER,
            LodestoneTrackerComponent(Optional.of<GlobalPos>(dest.toGlobalPos()), false)
        )
        compass.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Navigation Compass"))

        // for identification
        compass.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(
            NbtCompound().apply {
                putBoolean("qol_nav_compass", true)
            }
        ))
        return compass
    }
}