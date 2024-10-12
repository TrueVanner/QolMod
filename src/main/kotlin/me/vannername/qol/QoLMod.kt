package me.vannername.qol

import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.fzzyhmstrs.fzzy_config.api.RegisterType
import me.fzzyhmstrs.fzzy_config.config.Config
import me.fzzyhmstrs.fzzy_config.networking.api.ClientPlayNetworkContext
import me.fzzyhmstrs.fzzy_config.networking.api.ServerPlayNetworkContext
import me.vannername.qol.commands.AFKSetter
import me.vannername.qol.commands.EnderChestOpener
import me.vannername.qol.commands.Navigate
import me.vannername.qol.commands.SkipDayNight
import me.vannername.qol.config.PlayerConfig
import me.vannername.qol.config.ServerConfig
import me.vannername.qol.gui.MainGUI
import me.vannername.qol.utils.AFKPayload
import me.vannername.qol.utils.AFKPayloadCodec
import me.vannername.qol.utils.PlayerUtils.displayActionbarCoords
import me.vannername.qol.utils.PlayerUtils.displayNavCoords
import me.vannername.qol.utils.PlayerUtils.stopAFK
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Style
import net.minecraft.util.Identifier
import net.minecraft.util.dynamic.Codecs
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.properties.Delegates


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

//		MidnightConfig.init(MOD_ID, MidnightConfigExample::class.java)

        ConfigApi.network().registerS2C(CustomPayload.id<AFKPayload>("qolmod_afk_payload"), AFKPayloadCodec, ClientPacketReceiver::handleAFKPayload)
        ConfigApi.network().registerC2S(CustomPayload.id<AFKPayload>("qolmod_afk_payload"), AFKPayloadCodec, ServerPacketReceiver::handleAFKPayload)

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
            this.server = server
            defaultWorld = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.of("overworld")))!!
            for (world in server.worlds) {
                serverWorldIDs += world.registryKey.value
            }
        }
    }

    object ClientPacketReceiver {
        //insulating any client code that might be in ClientClassThatNeedsPayload
        fun handleAFKPayload(payload: AFKPayload, context: ClientPlayNetworkContext) {
            AFKMixinVariables.setIsAFK(payload.newAFKState)
        }
    }

    object ServerPacketReceiver {
        //insulating any client code that might be in ClientClassThatNeedsPayload
        fun handleAFKPayload(payload: AFKPayload, context: ServerPlayNetworkContext) {
            context.player().stopAFK()
        }
    }
}