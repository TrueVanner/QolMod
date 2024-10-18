package me.vannername.qol.client.utils

import net.minecraft.client.item.CompassAnglePredicateProvider
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.GlobalPos

// Deprecated until Renderer doesn't begin to work properly.
object RenderNavigationCompass {
    val angleProvider =
        CompassAnglePredicateProvider { world, _, _ -> GlobalPos(world.registryKey, BlockPos(0, 50, 0)) }

//    fun register() {
//        RenderEvents.HUD.register { matrices ->
//            val p = MinecraftClient.getInstance().player!!
//            if (p.hasConfig()) {
//                if (p.getConfig().navData.isNavigating) {
//                    renderCompass(matrices.matrices, angleProvider, p)
//                }
//            }
//        }
//    }

    private fun getCompassTextureID(angle: Float): String {
        return when {
            angle == 0f -> "16"
            angle < 0.015625f -> "17"
            angle < 0.046875f -> "18"
            angle < 0.078125f -> "19"
            angle < 0.109375f -> "20"
            angle < 0.140625f -> "21"
            angle < 0.171875f -> "22"
            angle < 0.203125f -> "23"
            angle < 0.234375f -> "24"
            angle < 0.265625f -> "25"
            angle < 0.296875f -> "26"
            angle < 0.328125f -> "27"
            angle < 0.359375f -> "28"
            angle < 0.390625f -> "29"
            angle < 0.421875f -> "30"
            angle < 0.453125f -> "31"
            angle < 0.484375f -> "00"
            angle < 0.515625f -> "01"
            angle < 0.546875f -> "02"
            angle < 0.578125f -> "03"
            angle < 0.609375f -> "04"
            angle < 0.640625f -> "05"
            angle < 0.671875f -> "06"
            angle < 0.703125f -> "07"
            angle < 0.734375f -> "08"
            angle < 0.765625f -> "09"
            angle < 0.796875f -> "10"
            angle < 0.828125f -> "11"
            angle < 0.859375f -> "12"
            angle < 0.890625f -> "13"
            angle < 0.921875f -> "14"
            angle < 0.953125f -> "15"
            else -> "16"
        }
    }

    private fun render(
        matrixStack: MatrixStack,
        angleProvider: CompassAnglePredicateProvider,
        p: ClientPlayerEntity
    ) {
        // TODO: beautify, change compass location + make it responsive to off-hand slot
        val angle = (angleProvider.unclampedCall(
            ItemStack(Items.COMPASS), p.clientWorld, p, 0
        ))

//        Renderer2d.renderTexture(
//            matrixStack,
//            Identifier.of("textures/item/compass_${getCompassTextureID(angle)}.png"),
//            100.0,
//            100.0,
//            16.0,
//            16.0
//        )
    }
}