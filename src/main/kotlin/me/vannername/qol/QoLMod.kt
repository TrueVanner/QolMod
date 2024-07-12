package me.vannername.qol

import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.vannername.qol.commands.EnderChestOpener
import me.vannername.qol.commands.Navigate
import me.vannername.qol.commands.SkipDayNight
import me.vannername.qol.config.PlayerConfig
import me.vannername.qol.config.ServerConfig
import me.vannername.qol.gui.MainGUI
import me.vannername.qol.utils.PlayerUtils.displayActionbarCoords
import me.vannername.qol.utils.PlayerUtils.displayNavCoords
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*


object QoLMod : ModInitializer {
    const val MOD_ID = "vannername-qol-mod"

    @JvmField
    val logger: Logger = LoggerFactory.getLogger(MOD_ID)

    // default world of the server. Is defined as overworld as soon as the server starts.
    @JvmField
    var defaultWorld: ServerWorld? = null

    @JvmField
    var serverWorldIDs: List<Identifier> = listOf()

    @JvmField
    var playerConfigs: Map<UUID, PlayerConfig> = mutableMapOf()

    val serverConfig = ConfigApi.registerAndLoadConfig({ ServerConfig() })

//    for testing
//    val serverConfig = ServerConfig()


    override fun onInitialize() {
        EnderChestOpener.init()
//        ConfigureProperty()
//        Testing()
        MainGUI()
        Navigate.init()
        SkipDayNight.init()

//		MidnightConfig.init(MOD_ID, MidnightConfigExample::class.java)

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

        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            defaultWorld = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier("overworld")))!!
            for (world in server.worlds) {
                serverWorldIDs += world.registryKey.value
            }
        }
    }
}