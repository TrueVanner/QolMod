package me.vannername.qol

//import me.x150.renderer.event.RenderEvents
//import me.x150.renderer.render.Renderer2d
import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.vannername.qol.client.utils.TPCreditsComputation
import me.vannername.qol.clientutils.AFKMixinVariables
import me.vannername.qol.clientutils.GlobalMixinVariables
import me.vannername.qol.main.config.PlayerConfig
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.item.CompassAnglePredicateProvider
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.item.Items

object QoLModClient : ClientModInitializer {

    override fun onInitializeClient() {
//        val angleProvider =
//            CompassAnglePredicateProvider { world, _, _ -> GlobalPos(world.registryKey, BlockPos(0, 50, 0)) }

        ClientPlayConnectionEvents.JOIN.register { _, _, client ->
            if (!client.isInSingleplayer) {

                val uuid = client.player!!.uuid
                QoLMod.playerConfigs += uuid to ConfigApi.registerAndLoadConfig({ PlayerConfig(uuid) })
            }

            // used for client-side AFK handling
            GlobalMixinVariables.setPlayerEnteredServer(true)
        }

        ClientTickEvents.END_WORLD_TICK.register { world ->
            for (p in world.players) {
                TPCreditsComputation.tick(p as ClientPlayerEntity, 5)
            }
        }

        ClientPlayConnectionEvents.DISCONNECT.register {_, client ->
            GlobalMixinVariables.setPlayerEnteredServer(false)
            AFKMixinVariables.setEnteredEscMenu(false)
        }

//        RenderEvents.HUD.register { matrices ->
//            val p = MinecraftClient.getInstance().player!!
//            if (p.hasConfig()) {
//                if (p.getConfig().navData.isNavigating) {
//                    renderCompass(matrices.matrices, angleProvider, p)
//                }
//            }
//        }
    }

//    private fun listenForAFKStart() {
//        if(me.vannername.qol.clientutils.GlobalMixinVariables.getPlayerConfig().isAFK) {
//            me.vannername.qol.clientutils.GlobalMixinVariables.setPlayerIsInvulnerable(true)
//        }
//    }
//
//    private fun listenForAFKEnd() {
//        if(!me.vannername.qol.clientutils.GlobalMixinVariables.getPlayerConfig().isAFK) {
//            me.vannername.qol.clientutils.GlobalMixinVariables.setPlayerIsInvulnerable(false)
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

    private fun renderCompass(
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