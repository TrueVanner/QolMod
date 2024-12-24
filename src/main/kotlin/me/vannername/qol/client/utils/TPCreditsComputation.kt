package me.vannername.qol.client.utils

import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.vannername.qol.main.QoLMod
import me.vannername.qol.main.commands.tptospawn.TPToSpawnUtils
import me.vannername.qol.main.networking.payloads.TPCreditsPayload
import me.vannername.qol.main.utils.PlayerUtils.sendSimpleMessage
import me.vannername.qol.main.utils.Utils
import me.vannername.qol.main.utils.WorldBlockPos
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.util.Formatting
import kotlin.math.abs

object TPCreditsComputation {
    var lastLocations: List<WorldBlockPos> = mutableListOf<WorldBlockPos>()

    fun storeLocation(p: AbstractClientPlayerEntity) {
        lastLocations += WorldBlockPos.ofPlayer(p)
    }

    fun computeDistances(): List<Double> {
        // return list of distances without the final element
        return lastLocations.mapIndexed { i, l ->
            if (i < lastLocations.size - 1) {
                // skip immediately if world transition has occurred
                if (!l.isInSameWorld(lastLocations[i + 1])) 0.0

                l.distanceTo(lastLocations[i + 1])
            } else {
                0.0
            }
        }.subList(0, lastLocations.size - 1)
    }

    fun computeRelations(): List<Double> {
        val size = computeDistances().size
        return computeDistances().mapIndexed { i, d ->
            if (i < size - 1) {
                if (d == 0.0 || computeDistances()[i + 1] == 0.0) {
                    return@mapIndexed 1.0
                }
                d / computeDistances()[i + 1]
            } else {
                0.0
            }
        }.subList(0, size - 1)
    }

    var lastTick: Long = 0

    /**
     * Determines if the given relation of the distances between the player's
     * last locations is sufficiently different from 1.
     */
    fun sufficientlyDifferent(distancesRelation: Double): Boolean {
        return abs(1 - distancesRelation) > 0.1
    }

    fun tick(p: AbstractClientPlayerEntity, secondsBetweenTicks: Double) {
        // only tick once per secondsBetweenTicks seconds
        if (System.currentTimeMillis() - lastTick < secondsBetweenTicks * 1000) {
            return
        }
        // update the variable when ticked
        lastTick = System.currentTimeMillis()

        // store current player location
        storeLocation(p)

        val ticksPerMinute = 60.0 / secondsBetweenTicks

        // regardless of time between ticks, credits are given out each minute
        if (lastLocations.size >= ticksPerMinute) {
            try {
                Utils.debug("Computing TP credits for player: ${p.name.string}")
                Utils.debug("Last locations: $lastLocations")
                // compute credits reward: 0.1 credit per minute is the max reward
                // one can get.  for each relation sufficiently
                // different from 1 (only possible if player movement varied
                // significantly during the computation)

                val relations = computeRelations()
                Utils.debug("Relations: ${relations.map { it.toString() + if (sufficientlyDifferent(it)) " (+)" else "" }}")

                val creditsReward = computeRelations().fold(0.0) { acc, it ->
                    acc + if (sufficientlyDifferent(it)) 0.125 / (ticksPerMinute - 2) else 0.0
                }

                Utils.debug("Credits reward: $creditsReward")

                // send a request to update player's TP credits to the server
                ConfigApi.network().send(TPCreditsPayload(TPToSpawnUtils.TPCredits(creditsReward)), p)
            } catch (e: Exception) {
                p.sendSimpleMessage(
                    "Warning: failed to compute TP Credits for the last minute. If this continues please contact админ (хуесос) asap!!",
                    Formatting.RED
                )
                e.printStackTrace()
                QoLMod.logger.debug("Warning: failed to compute TP Credits")
            }

            // empty the list
            lastLocations = mutableListOf()
        }
    }
}