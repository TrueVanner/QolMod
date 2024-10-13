package me.vannername.qol.networking

import me.vannername.qol.main.networking.NetworkingUtils
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

data class AFKPayload(var newAFKState: Boolean) : CustomPayload {
    override fun getId(): CustomPayload.Id<out CustomPayload?>? {
        return NetworkingUtils.getCustomID(AFKPayload::class)
    }

    object AFKPayloadCodec : PacketCodec<RegistryByteBuf, AFKPayload> {
        override fun encode(buf: RegistryByteBuf, payload: AFKPayload) {
            buf.writeBoolean(payload.newAFKState)
        }

        override fun decode(buf: RegistryByteBuf): AFKPayload {
            return AFKPayload(buf.readBoolean())
        }
    }
}

