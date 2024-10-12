package me.vannername.qol.utils

import net.minecraft.network.packet.CustomPayload

// right now, newAFKState is never used - it's always true from the server and always false from the client.
class AFKPayload(val newAFKState: Boolean): CustomPayload {
    override fun getId(): CustomPayload.Id<out CustomPayload?>? {
        return CustomPayload.id<AFKPayload>("qolmod_afk_payload")
    }

}