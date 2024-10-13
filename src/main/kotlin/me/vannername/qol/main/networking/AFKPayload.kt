package me.vannername.qol.networking

import net.minecraft.network.packet.CustomPayload

class AFKPayload(val newAFKState: Boolean): CustomPayload {
    override fun getId(): CustomPayload.Id<out CustomPayload?>? {
        return CustomPayload.id<AFKPayload>("qolmod_afk_payload")
    }
}