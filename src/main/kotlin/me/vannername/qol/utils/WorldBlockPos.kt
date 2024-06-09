package me.vannername.qol.utils

import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Represents a position in the world. Serves as an extension of BlockPos.
 */
open class WorldBlockPos(x: Int, y: Int, z: Int, worldKey: RegistryKey<World>) : BlockPos(x, y, z) {
    val worldID = worldKey.value

    constructor(pos: BlockPos, worldID: RegistryKey<World>) : this(pos.x, pos.y, pos.z, worldID)
    constructor(x: Double, y: Double, z: Double, worldID: RegistryKey<World>) : this(
        BlockPos(
            x.toInt(),
            y.toInt(),
            z.toInt()
        ), worldID
    )

    fun getWorld(server: MinecraftServer): ServerWorld {
        return server.getWorld(RegistryKey.of(RegistryKeys.WORLD, worldID))!!
    }

    fun getDistance(l: WorldBlockPos): Double {
        if (l.worldID == this.worldID) {
            return sqrt(
                (x - l.x).toDouble().pow(2) + (y - l.y).toDouble().pow(2) + (z - l.z).toDouble()
                    .pow(2)
            )
        } else throw RuntimeException("Attempted to compute distance across different worlds")
    }

    /**
     * Checks if the world is equal to the world of this WorldBlockPos.
     */
    fun isInSameWorld(world: World): Boolean {
        return worldID == world.registryKey.value
    }
}