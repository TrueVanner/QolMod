package me.vannername.qol.networking

import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec

object AFKPayloadCodec : PacketCodec<RegistryByteBuf, AFKPayload> {
    override fun encode(buf: RegistryByteBuf, payload: AFKPayload) {
        buf.writeBoolean(payload.newAFKState)
    }

    override fun decode(buf: RegistryByteBuf): AFKPayload {
        return AFKPayload(buf.readBoolean())
    }
}