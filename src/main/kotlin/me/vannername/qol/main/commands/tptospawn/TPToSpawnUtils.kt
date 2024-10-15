package me.vannername.qol.main.commands.tptospawn

import me.vannername.qol.main.utils.PlayerUtils.getConfig
import me.vannername.qol.main.utils.PlayerUtils.sendSimpleMessage
import me.vannername.qol.main.utils.WorldBlockPos
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Formatting
import java.text.DecimalFormat

object TPToSpawnUtils {

    sealed class TeleportData {
        data class Success(val to: WorldBlockPos, val cost: Double) : TeleportData()
        data class Error(val errorMessage: String) : TeleportData()
    }

    // 1 TP credit = 1000 blocks
    fun calculateTPCost(distance: Double): Double {
        return distance / 1000
    }

    fun stringOf(credits: Double): String {
        val format = DecimalFormat("0.##")
        return format.format(credits)
    }

    fun ServerPlayerEntity.checkTeleportRequirements(): TeleportData {
        // basic first checks
        val errorMessage = when {
            !isOnGround -> "You can't teleport while in the air!"
            getConfig().isAFK -> "You can't teleport while AFK!"
            vehicle != null -> "You can't teleport while in a vehicle!"
            else -> null
        }

        if (errorMessage != null) {
            return TeleportData.Error(errorMessage)
        }

        val destination = spawnPointPosition
            .takeIf { it != null && spawnPointDimension == world.registryKey }
            ?: world.getSpawnPos().also {
                sendSimpleMessage(
                    "Warning: your spawn point in this world doesn't exist, so you will be sent to the world spawn.",
                    Formatting.YELLOW
                )
            }

        if (WorldBlockPos(destination, world.registryKey).isWithinDistance(WorldBlockPos.ofPlayer(this), 10.0)) {
            return TeleportData.Error("You're already there!")
        }

        // TODO check if there is enough space for the player to stay in teleport location
//        if(world.isSpaceEmpty(this, this.boundingBox.offset(destination)))

        // TODO: import cost calculations
        val currentCredits = getConfig().tpCredits
        val cost = calculateTPCost(WorldBlockPos.ofPlayer(this).distanceToBlockPos(destination))

        if (currentCredits < cost) {
            return TeleportData.Error(
                "You don't have enough TP credits to teleport!\nRequired: ${stringOf(cost)} TPC | You have: ${
                    stringOf(
                        currentCredits
                    )
                } (${stringOf(currentCredits - cost)}) TPC"
            )
        }

        return TeleportData.Success(WorldBlockPos(destination, world.registryKey), cost)
    }
}