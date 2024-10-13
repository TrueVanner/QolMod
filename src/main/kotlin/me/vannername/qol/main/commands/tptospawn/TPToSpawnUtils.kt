package me.vannername.qol.main.commands.tptospawn

import me.vannername.qol.main.utils.PlayerUtils.getConfig
import me.vannername.qol.main.utils.WorldBlockPos
import net.minecraft.server.network.ServerPlayerEntity

object TPToSpawnUtils {

    sealed class TeleportData {
        data class Success(val to: WorldBlockPos, val cost: Int, val message: String?) : TeleportData()
        data class Error(val errorMessage: String) : TeleportData()
    }

    fun ServerPlayerEntity.checkTeleportRequirements(): TeleportData {
        val errorMessage = when {
            !isOnGround -> "You can't teleport while in the air!"
            getConfig().isAFK -> "You can't teleport while AFK!"
            vehicle != null -> "You can't teleport while in a vehicle!"
            else -> null
        }

        if (errorMessage != null) {
            return TeleportData.Error(errorMessage)
        }

        var message: String? = null
        val destination = spawnPointPosition
            .takeIf { it != null && spawnPointDimension == world.registryKey }
            ?: world.getSpawnPos().also {
                message =
                    "Warning: your spawn point in this world didn't exist, so you were sent to the world spawn."
            }

        // TODO check if there is enough space for the player to stay in teleport location
//        if(world.isSpaceEmpty(this, this.boundingBox.offset(destination)))


        // TODO: import cost calculations
        val cost = 5

        if (totalExperience < cost) {
            return TeleportData.Error("You don't have enough experience to teleport!\nYour experience: $totalExperience\nRequired experience: $cost (you lack ${totalExperience - cost} points)")
        }

        return TeleportData.Success(WorldBlockPos(destination, world.registryKey), cost, message)
    }
}