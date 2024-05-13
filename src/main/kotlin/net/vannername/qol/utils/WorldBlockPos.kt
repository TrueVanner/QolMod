package net.vannername.qol.utils

import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionOptions
import net.minecraft.world.dimension.DimensionType
import kotlin.math.pow
import kotlin.math.sqrt

class WorldBlockPos(x: Int, y: Int, z: Int, var worldID: Identifier) : BlockPos(x, y, z)
{
    constructor(x: Double, y: Double, z: Double, worldID: Identifier) : this(x.toInt(), y.toInt(), z.toInt(), worldID)

    fun getWorld(server: MinecraftServer): ServerWorld {
        return server.getWorld(RegistryKey.of(RegistryKeys.WORLD, worldID))!!
    }

    fun getDistance(l: WorldBlockPos): Double {
        if(l.worldID == this.worldID) {
            return sqrt((x - l.x).toDouble().pow(2) + (y - l.y).toDouble().pow(2) + (z - l.z).toDouble().pow(2))
        } else throw RuntimeException("Attempted to compute distance across worlds")
    }
}