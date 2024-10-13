package me.vannername.qol.main.networking

import net.minecraft.network.packet.CustomPayload
import kotlin.reflect.KClass

object NetworkingUtils {
    fun <T : CustomPayload> getCustomID(payloadClass: KClass<T>): CustomPayload.Id<T> {
        return CustomPayload.id<T>("qolmod_${payloadClass.simpleName!!.toLowerCase()}")
    }
}