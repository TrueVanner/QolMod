package me.vannername.qol

import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.vannername.qol.commands.ConfigureProperty
import me.vannername.qol.commands.EnderChestOpener
import me.vannername.qol.commands.Navigate
import me.vannername.qol.commands.Testing
import me.vannername.qol.gui.MainGUI
import me.vannername.qol.utils.PlayerConfig
import me.vannername.qol.utils.PlayerUtils.displayActionbarCoords
import me.vannername.qol.utils.PlayerUtils.displayNavCoords
import me.vannername.qol.utils.ServerConfig
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*


object QoLMod : ModInitializer {
    const val MOD_ID = "vannername-qol-mod"

    @JvmField
    val logger: Logger = LoggerFactory.getLogger(MOD_ID)

    @JvmField
    var playerConfigs: Map<UUID, PlayerConfig> = mutableMapOf()
    val serverConfig = ConfigApi.registerAndLoadConfig({ ServerConfig() })

    override fun onInitialize() {
//		MidnightConfig.init(MOD_ID, MidnightConfigExample::class.java)
        EnderChestOpener()
        ConfigureProperty()
        Testing()
        MainGUI()
        Navigate()

        ServerPlayConnectionEvents.JOIN.register { networkHandler, _, _ ->
            val uuid = networkHandler.player.uuid
            playerConfigs += uuid to ConfigApi.registerAndLoadConfig({ PlayerConfig(uuid) })
        }

        ServerTickEvents.END_WORLD_TICK.register { world ->
            for (p in world.players) {
                p.displayActionbarCoords()
                p.displayNavCoords()
            }
        }
    }
}