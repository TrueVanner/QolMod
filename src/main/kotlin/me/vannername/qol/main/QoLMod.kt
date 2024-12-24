package me.vannername.qol.main

import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.vannername.qol.commands.util.GetCoords
import me.vannername.qol.commands.util.Help
import me.vannername.qol.main.commands.EnderChestOpener
import me.vannername.qol.main.commands.afk.AFKSetter
import me.vannername.qol.main.commands.navigate.Navigate
import me.vannername.qol.main.commands.serverchest.ServerChestCmd
import me.vannername.qol.main.commands.serverchest.ServerChestUtils
import me.vannername.qol.main.commands.skipdaynight.SkipDayNight
import me.vannername.qol.main.commands.tptospawn.TeleportToSpawn
import me.vannername.qol.main.config.PlayerConfig
import me.vannername.qol.main.config.ServerConfig
import me.vannername.qol.main.items.ModItems
import me.vannername.qol.main.networking.NetworkingUtils
import me.vannername.qol.main.networking.ServerPacketReceiver
import me.vannername.qol.main.networking.payloads.AFKPayload
import me.vannername.qol.main.networking.payloads.AFKPayload.AFKPayloadCodec
import me.vannername.qol.main.networking.payloads.TPCreditsPayload
import me.vannername.qol.main.utils.PlayerUtils.displayActionbarCoords
import me.vannername.qol.main.utils.PlayerUtils.displayNavCoords
import me.vannername.qol.main.utils.PlayerUtils.lightUpNearestInvisibleItemFrames
import me.vannername.qol.main.utils.Utils
import me.vannername.qol.networking.ClientPacketReceiver
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.entity.decoration.ItemFrameEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.particle.ParticleTypes
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

    fun registerCommands() {
//        MainGUI()
        EnderChestOpener.init()
        Navigate.init()
        SkipDayNight.init()
        AFKSetter.init()
        TeleportToSpawn.init()
        GetCoords.init()
        ServerChestCmd.init()
        Help.init()
    }

    fun registerEvents() {

        fun handleHotbarCoordinates() {
            ServerTickEvents.END_WORLD_TICK.register { world ->
                for (p in world.players) {
                    p.displayActionbarCoords() // displays the coordinates of the curre
                    p.displayNavCoords()
                }
            }
        }

        fun addPlayerConfigUponJoining() {
            ServerPlayConnectionEvents.JOIN.register { networkHandler, _, _ ->
                // add the player to the config
                val uuid = networkHandler.player.uuid
                playerConfigs += uuid to ConfigApi.registerAndLoadConfig({ PlayerConfig(uuid) })
                // update the AFK status of the player
                ConfigApi.network().send(AFKPayload(playerConfigs[uuid]!!.isAFK), networkHandler.player)
            }
        }

        fun setVariablesOnServerLoad() {
            ServerLifecycleEvents.SERVER_STARTED.register { server ->
                this.server = server
                defaultWorld = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.of("overworld")))!!

                for (world in server.worlds) {
                    serverWorldIDs += world.registryKey.value
                }
            }
        }

        fun saveServerChestOnStop() {
            ServerLifecycleEvents.SERVER_STOPPING.register { server ->
                ServerChestUtils.saveServerChest()
            }
        }

        fun itemFrameHandling() {
            ServerEntityEvents.ENTITY_LOAD.register { entity, world ->
                if (entity is ItemFrameEntity) {
                    if (world.getEntitiesByClass<PlayerEntity>(
                            PlayerEntity::class.java,
                            entity.boundingBox.expand(5.0),
                            { player -> player.offHandStack.isOf(Items.AMETHYST_SHARD) }
                        ).isNotEmpty()
                    ) {
                        entity.isInvisible = true
                    }
//            System.out.println("Item frame placed at: " + itemFrame.getBlockPos());
                    // Your custom logic here
                }
            }
        }

        fun handleItemFrameVisibility() {
            ServerTickEvents.END_WORLD_TICK.register { world ->
                for (p in world.players) {
                    p.lightUpNearestInvisibleItemFrames()
                }
            }
        }

        fun preventFriendlyDamage() {
            ServerLivingEntityEvents.ALLOW_DAMAGE.register { entity, source, amount ->
                if (source.source is PlayerEntity && source.source?.isSneaking == true && !Utils.hostiles.contains(
                        entity.type
                    )
                ) {
                    source.source!!.world.addParticle(
                        ParticleTypes.HEART,
                        entity.x, // - 0.5 + Math.random(),
                        entity.y + entity.height + 2,// - 0.25 + Math.random() * 0.5,
                        entity.z, // - 0.5 + Math.random(),
                        0.0, 0.0, 0.0
                    )
                    false
                } else {
                    true
                }
            }
        }

        handleHotbarCoordinates()
        addPlayerConfigUponJoining()
        setVariablesOnServerLoad()
        saveServerChestOnStop()
        itemFrameHandling()
        handleItemFrameVisibility()
        preventFriendlyDamage()
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

    override fun onInitialize() {
        registerCommands()
        registerNetworkHandlers()
        registerEvents()

        ModItems.init()
    }
}