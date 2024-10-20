package me.vannername.qol.main.networking.payloads

import me.vannername.qol.main.commands.tptospawn.TPToSpawnUtils
import me.vannername.qol.main.networking.NetworkingUtils
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

data class TPCreditsPayload(var addedCredits: TPToSpawnUtils.TPCredits) : CustomPayload {
    override fun getId(): CustomPayload.Id<out CustomPayload?>? {
        return NetworkingUtils.getCustomID(TPCreditsPayload::class)
    }

    object TPCreditsPayloadCodec : PacketCodec<RegistryByteBuf, TPCreditsPayload> {
        override fun encode(buf: RegistryByteBuf, payload: TPCreditsPayload) {
            buf.writeDouble(payload.addedCredits.value)
        }

        override fun decode(buf: RegistryByteBuf): TPCreditsPayload {
            return TPCreditsPayload(TPToSpawnUtils.TPCredits(buf.readDouble()))
        }
    }
}

