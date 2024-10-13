package me.vannername.qol.main.commands.navigate

import me.vannername.qol.main.config.PlayerConfig
import me.vannername.qol.main.utils.PlayerUtils.getConfig
import me.vannername.qol.main.utils.WorldBlockPos
import net.minecraft.server.network.ServerPlayerEntity
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

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
    }

    fun ServerPlayerEntity.stopNavigation() {
        getConfig().navData.isNavigating = false
    }

    /**
     * Only works if the server has the [Coord Finder](https://modrinth.com/mod/coord-finder/version/fabric-1.20.6-1.1.0) mod installed.
     * Transforms the positions specified in the config file of the mod to the list of positions in the current mod's format.
     *
     * @return the list of the positions on the server stored in positions.properties
     */
    fun decomposeCoordsLocations(): Map<String, WorldBlockPos> {
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
}