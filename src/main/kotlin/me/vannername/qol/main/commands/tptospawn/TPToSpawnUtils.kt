package me.vannername.qol.main.commands.tptospawn

import me.vannername.qol.main.utils.PlayerUtils.getConfig
import me.vannername.qol.main.utils.PlayerUtils.sendSimpleMessage
import me.vannername.qol.main.utils.WorldBlockPos
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Formatting
import java.text.DecimalFormat

object TPToSpawnUtils {

    sealed class TeleportData(val dest: Destination) {
        data class Success(private val to: Destination, val cost: TPCredits) : TeleportData(to)

        // destination is also included in the error message to simplify querying
        data class Error(private val to: Destination, val errorMessage: String) : TeleportData(to)
    }

    data class TPCredits(val value: Double) {
        operator fun compareTo(other: TPCredits): Int {
            return this.value.compareTo(other.value)
        }

        operator fun minus(other: TPCredits): TPCredits {
            return TPCredits(this.value - other.value)
        }

        operator fun plus(other: TPCredits): TPCredits {
            return TPCredits(this.value + other.value)
        }

        private val format = DecimalFormat("0.##")

        override fun toString(): String {
            return format.format(this.value)
        }

        /**
         * @return the fraction of the total credits this number of credits represents.
         */
        fun getFraction(total: TPCredits): String {
            return "${format.format(this.value / total.value)}%"
        }
    }

    data class Destination(val to: WorldBlockPos, val toSpawn: Boolean)

    // 1 TP credit = 1000 blocks
    fun calculateTPCost(distance: Double): TPCredits {
        return TPCredits(distance / 1000)
    }

    /**
     * @return either the player's spawn location and true if the player
     * has a spawn-point in their current world, or player's current world's
     * spawn and false otherwise.
     */
    fun ServerPlayerEntity.getTeleportDestination(): Destination {
        return if (spawnPointPosition != null && spawnPointDimension == world.registryKey) {
            Destination(WorldBlockPos(spawnPointPosition!!, world.registryKey), true)
        } else {
            Destination(WorldBlockPos(world.getSpawnPos(), world.registryKey), false)
        }
    }

    fun ServerPlayerEntity.checkTeleportRequirements(): TeleportData {
        // basic first checks
        val errorMessage = when {
            !isOnGround -> "You can't teleport while in the air!"
            getConfig().isAFK -> "You can't teleport while AFK!"
            vehicle != null -> "You can't teleport while in a vehicle!"
            else -> null
        }

        val destination = getTeleportDestination().also {
            if (!it.toSpawn) {
                sendSimpleMessage(
                    "Warning: your spawn point in this world doesn't exist, so you will be sent to the world spawn.",
                    Formatting.YELLOW
                )
            }
        }

        if (errorMessage != null) {
            return TeleportData.Error(destination, errorMessage)
        }

        if (destination.to.isWithinDistance(WorldBlockPos.ofPlayer(this), 10.0)) {
            return TeleportData.Error(destination, "You're already there!")
        }

        // TODO check if there is enough space for the player to stay in teleport location
//        if(world.isSpaceEmpty(this, this.boundingBox.offset(destination)))

        // TODO: import cost calculations
        val currentCredits = TPCredits(getConfig().tpCredits)
        val cost = calculateTPCost(WorldBlockPos.ofPlayer(this).distanceTo(destination.to))

        if (currentCredits < cost) {
            return TeleportData.Error(
                destination,
                "You don't have enough TP credits to teleport!\nRequired: $cost TPC | You have: $currentCredits (${currentCredits - cost}) TPC"
            )
        }

        return TeleportData.Success(destination, cost)
    }
}