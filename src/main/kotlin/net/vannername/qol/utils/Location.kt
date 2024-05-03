package net.vannername.qol.utils

import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import kotlin.math.pow
import kotlin.math.sqrt

class Location(
    val x: Double, val y: Double, val z: Double, val worldName: String
)
{
    constructor(x: Int, y: Int, z: Int, worldName: String) : this(x.toDouble(), y.toDouble(), z.toDouble(), worldName)
    fun getWorld(server: MinecraftServer): ServerWorld {
        return server.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier(worldName)))!!
    }

    fun getDistance(l: Location): Double {
        return sqrt((x - l.x).pow(2) + (y - l.y).pow(2) + (z - l.z).pow(2))
    }
}