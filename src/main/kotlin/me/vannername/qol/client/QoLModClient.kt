package me.vannername.qol

//import me.x150.renderer.event.RenderEvents
//import me.x150.renderer.render.Renderer2d
import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.vannername.qol.client.utils.TPCreditsComputation
import me.vannername.qol.clientutils.AFKMixinVariables
import me.vannername.qol.clientutils.GlobalMixinVariables
import me.vannername.qol.main.QoLMod
import me.vannername.qol.main.config.PlayerConfig
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents

object QoLModClient : ClientModInitializer {

    override fun onInitializeClient() {

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
                TPCreditsComputation.tick(p, 5.0)
            }
        }

        ClientPlayConnectionEvents.DISCONNECT.register {_, client ->
            GlobalMixinVariables.setPlayerEnteredServer(false)
            AFKMixinVariables.setEnteredEscMenu(false)
        }

//        RenderNavigationCompass.register()
    }
}