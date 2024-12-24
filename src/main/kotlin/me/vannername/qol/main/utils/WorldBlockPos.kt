package me.vannername.qol.main.utils

import me.fzzyhmstrs.fzzy_config.util.Walkable
import me.fzzyhmstrs.fzzy_config.validation.collection.ValidatedList
import me.fzzyhmstrs.fzzy_config.validation.collection.ValidatedMap
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedString
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.GlobalPos
import net.minecraft.world.World
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Represents a position in the world. Serves as an extension of BlockPos.
 */
open class WorldBlockPos(x: Int, y: Int, z: Int, val worldID: String) : BlockPos(x, y, z), Walkable {

    constructor() : this(0, 0, 0, "minecraft:overworld")
    constructor(x: Int, y: Int, z: Int, worldKey: RegistryKey<World>) : this(x, y, z, worldKey.value.toString())
    constructor(pos: BlockPos, worldID: RegistryKey<World>) : this(pos.x, pos.y, pos.z, worldID)
    constructor(x: Double, y: Double, z: Double, worldID: RegistryKey<World>) : this(
        BlockPos(
            x.toInt(),
            y.toInt(),
            z.toInt()
        ), worldID
    )

    companion object {
        fun ofPlayer(p: PlayerEntity): WorldBlockPos {
            return WorldBlockPos(p.blockPos, p.world.registryKey)
        }
    }

    fun getWorld(server: MinecraftServer): ServerWorld {
        return server.getWorld(getWorldRegistryKey())!!
    }

    fun getWorldRegistryKey(): RegistryKey<World> {
        return RegistryKey.of(RegistryKeys.WORLD, Identifier.of(worldID))
    }

    /**
     * Determines the distance between this position and a BlockPos in the same world.
     */
    fun distanceToBlockPos(l: BlockPos): Double {
        return sqrt(
            (x - l.x).toDouble().pow(2) + (y - l.y).toDouble().pow(2) + (z - l.z).toDouble()
                .pow(2)
        )
    }

    /**
     * Determines the distance between this and another WorldBlockPos.
     * @throws RuntimeException if the worlds are different.
     */
    fun distanceTo(l: WorldBlockPos): Double {
        if (l.worldID != this.worldID)
            throw RuntimeException("Attempted to compute distance across different worlds")

        return distanceToBlockPos(l)
    }

    fun isWithinDistance(pos: WorldBlockPos, distance: Double): Boolean {
        return distanceTo(pos) <= distance
    }


    fun getString(includeWorld: Boolean = false): String {
        return (if (includeWorld) worldID else "") + "$x $y $z"
    }

    /**
     * Checks if the world is equal to the world of this WorldBlockPos.
     */
    fun isInSameWorld(world: World): Boolean {
        return worldID == world.registryKey.value.toString()
    }

    /**
     * Checks if the world is equal to the world of this WorldBlockPos.
     */
    fun isInSameWorld(world: Identifier): Boolean {
        return worldID == world.toString()
    }

    fun isInSameWorld(pos: WorldBlockPos): Boolean {
        return worldID == pos.worldID
    }

    fun toGlobalPos(): GlobalPos {
        return GlobalPos(getWorldRegistryKey(), this)
    }

    // fix: coming up with it was tough and weird, but it fixes the problem of ValidatedAny
    // not being able to configure BlockPos. In the future, best to check of the problem
    // disappears or there is an official solution
    class Validated(pos: WorldBlockPos) : ValidatedMap<List<Int>, String>(
        mapOf(listOf(pos.x, pos.y, pos.z) to pos.worldID),
        ValidatedList(listOf(pos.x, pos.y, pos.z), ValidatedInt()),
        ValidatedString()
    ) {
        fun getWBP(): WorldBlockPos {
            val pos = storedValue.keys.iterator().next()
            val worldID = storedValue.values.iterator().next()
            return WorldBlockPos(pos[0], pos[1], pos[2], worldID)
        }
    }
}

