package me.vannername.qol.event;

import net.minecraft.entity.Entity
import net.minecraft.entity.decoration.ItemFrameEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.server.world.ServerWorld

object ItemFrameEventHandler {
    fun onEntityJoinWorld(entity: Entity, world: ServerWorld) {
        // Check if the entity is an ItemFrameEntity
        if (entity is ItemFrameEntity) {
            if (world.getEntitiesByClass<PlayerEntity>(
                    PlayerEntity::class.java,
                    entity.boundingBox.expand(5.0),
                    { player -> player.offHandStack.isOf(Items.AMETHYST_SHARD) }
                ).isNotEmpty()
            ) {
                entity.isInvisible = true
            }
//            System.out.println("Item frame placed at: " + itemFrame.getBlockPos());
            // Your custom logic here
        }
    }
}