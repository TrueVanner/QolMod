package net.vannername.qol.utils

import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import kotlin.math.pow
import kotlin.math.sqrt

class Location(
    val x: Int, val y: Int, val z: Int, val worldName: String
)
{
    constructor(x: Double, y: Double, z: Double, worldName: String) : this(x.toInt(), y.toInt(), z.toInt(), worldName)

    fun getWorld(server: MinecraftServer): ServerWorld {
        return server.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier(worldName)))!!
    }

    fun getDistance(l: Location): Double {
        return sqrt((x - l.x).toDouble().pow(2) + (y - l.y).toDouble().pow(2) + (z - l.z).toDouble().pow(2))
    }
}