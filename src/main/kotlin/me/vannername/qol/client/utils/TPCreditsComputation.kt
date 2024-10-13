package me.vannername.qol.client.utils

import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.vannername.qol.QoLMod
import me.vannername.qol.main.utils.WorldBlockPos
import me.vannername.qol.networking.TPCreditsPayload
import net.minecraft.client.network.ClientPlayerEntity
import kotlin.math.abs

object TPCreditsComputation {
    var lastLocations: List<WorldBlockPos> = mutableListOf<WorldBlockPos>()

    fun storeLocation(p: ClientPlayerEntity) {
        lastLocations += WorldBlockPos.current(p)
    }

    fun computeDistances(): List<Double> {
        // return list of distances without the final element
        return lastLocations.mapIndexed { i, l ->
            if (i < lastLocations.size - 1) {
                l.getDistance(lastLocations[i + 1])
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

    fun tick(p: ClientPlayerEntity, secondsBetweenTicks: Int) {
        // only tick once per secondsBetweenTicks seconds
        if (System.currentTimeMillis() - lastTick < secondsBetweenTicks * 1000) {
            return
        }
        // update the variable when ticked
        lastTick = System.currentTimeMillis()
        // store current player location
        storeLocation(p)

        // p.sendDebugMessage(lastLocations.last())

        // after a minute of location recording
        if (lastLocations.size == 60 / secondsBetweenTicks) {
            try {

            } catch (e: Exception) {
                QoLMod.logger.debug("Warning: failed to compute TP Credits")
            }

            // compute credits reward: 1 credit for each relation sufficiently
            // different from 1 (only possible if player movement varied
            // significantly during the computation)

            val creditsReward = computeRelations().fold(0.0) { acc, it ->
                if (abs(1 - it) > 0.25) 1.0 else 0.0
            }

            ConfigApi.network().send(TPCreditsPayload(creditsReward), p)

            lastLocations = mutableListOf()
        }
    }
}