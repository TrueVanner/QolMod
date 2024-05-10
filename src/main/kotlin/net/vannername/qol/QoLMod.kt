package net.vannername.qol

import com.mojang.blaze3d.systems.RenderSystem
import eu.pb4.playerdata.api.storage.JsonDataStorage
import eu.pb4.playerdata.api.storage.PlayerDataStorage
import me.x150.renderer.Renderer
import me.x150.renderer.event.RenderEvents
import me.x150.renderer.objfile.ObjFile
import me.x150.renderer.render.Renderer2d
import me.x150.renderer.util.RendererUtils
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarted
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.CompassAnglePredicateProvider
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.session.telemetry.WorldLoadedEvent
import net.minecraft.client.texture.TextureManager
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.CompassItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.MinecraftServer
import net.minecraft.test.TestFunctions
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.GlobalPos
import net.vannername.qol.commands.*
import net.vannername.qol.gui.MainGUI
import net.vannername.qol.schemes.PlayerData
import net.vannername.qol.utils.PlayerUtils
import net.vannername.qol.utils.PlayerUtils.displayActionbarCoords
import net.vannername.qol.utils.Utils
import net.vannername.qol.utils.Utils.getPlayerData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.jvm.optionals.getOrNull
import kotlin.math.max


object QoLMod : ModInitializer {
	const val MOD_ID = "vannername-qol-mod"
	@JvmField
	val logger: Logger = LoggerFactory.getLogger(MOD_ID)
	@JvmField
	var DATA_STORAGE: PlayerDataStorage<PlayerData> = JsonDataStorage("qolmodparams", PlayerData::class.java)

	override fun onInitialize() {
//		MidnightConfig.init(MOD_ID, MidnightConfigExample::class.java)
		EnderChestOpener()
		ConfigureProperty()
		Testing()
		MainGUI()

		ServerTickEvents.END_WORLD_TICK.register { world ->
			for (p in world.players) {
				p.displayActionbarCoords()
			}
		}


//		ServerStarted { server ->
//			Utils.debug(server.resourceManager.getResource(Identifier("item/compass_00")), "resource test")
//		}
	}
}