package me.vannername.qol

import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.vannername.qol.commands.util.GetCoords
import me.vannername.qol.main.commands.EnderChestOpener
import me.vannername.qol.main.commands.SkipDayNight
import me.vannername.qol.main.commands.afk.AFKSetter
import me.vannername.qol.main.commands.navigate.Navigate
import me.vannername.qol.main.commands.serverchest.ServerChest
import me.vannername.qol.main.commands.serverchest.ServerChestUtils
import me.vannername.qol.main.commands.tptospawn.TeleportToSpawn
import me.vannername.qol.main.config.PlayerConfig
import me.vannername.qol.main.config.ServerConfig
import me.vannername.qol.main.gui.MainGUI
import me.vannername.qol.main.networking.NetworkingUtils
import me.vannername.qol.main.utils.PlayerUtils.displayActionbarCoords
import me.vannername.qol.main.utils.PlayerUtils.displayNavCoords
import me.vannername.qol.networking.AFKPayload
import me.vannername.qol.networking.AFKPayload.AFKPayloadCodec
import me.vannername.qol.networking.ClientPacketReceiver
import me.vannername.qol.networking.ServerPacketReceiver
import me.vannername.qol.networking.TPCreditsPayload
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.MinecraftServer
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

    private lateinit var server: MinecraftServer

    fun getServer(): MinecraftServer {
        return server
    }

    @JvmField
    var playerConfigs: Map<UUID, PlayerConfig> = mutableMapOf()

    val serverConfig = ConfigApi.registerAndLoadConfig({ ServerConfig() })

//    for testing
//    val serverConfig = ServerConfig()


    override fun onInitialize() {
        MainGUI()
        EnderChestOpener.init()
        Navigate.init()
        SkipDayNight.init()
        AFKSetter.init()
        TeleportToSpawn.init()
        GetCoords.init()
        ServerChest.init()

//		MidnightConfig.init(MOD_ID, MidnightConfigExample::class.java)

        registerNetworkHandlers()

        ServerPlayConnectionEvents.JOIN.register { networkHandler, _, _ ->
            // add the player to the config
            val uuid = networkHandler.player.uuid
            playerConfigs += uuid to ConfigApi.registerAndLoadConfig({ PlayerConfig(uuid) })
            // update the AFK status of the player
            ConfigApi.network().send(AFKPayload(playerConfigs[uuid]!!.isAFK), networkHandler.player)
        }

        ServerTickEvents.END_WORLD_TICK.register { world ->
            for (p in world.players) {
                p.displayActionbarCoords()
                p.displayNavCoords()
            }
        }

        ServerLifecycleEvents.SERVER_STOPPING.register { server ->
            ServerChestUtils.serializeServerChest()
        }

        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            this.server = server
            defaultWorld = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.of("overworld")))!!

            for (world in server.worlds) {
                serverWorldIDs += world.registryKey.value
            }
        }
    }

    fun registerNetworkHandlers() {
        fun registerAFKPayload() {
            ConfigApi.network().registerC2S(
                NetworkingUtils.getCustomID(AFKPayload::class), AFKPayloadCodec,
                ServerPacketReceiver::handleAFKPayload
            )

            ConfigApi.network().registerS2C(
                NetworkingUtils.getCustomID(AFKPayload::class), AFKPayloadCodec,
                ClientPacketReceiver::handleAFKPayload
            )
        }

        fun registerTPCreditsPayload() {
            ConfigApi.network().registerC2S(
                NetworkingUtils.getCustomID(TPCreditsPayload::class),
                TPCreditsPayload.TPCreditsPayloadCodec,
                ServerPacketReceiver::handleTPCreditsPayload
            )
        }

        registerAFKPayload()
        registerTPCreditsPayload()
    }
}