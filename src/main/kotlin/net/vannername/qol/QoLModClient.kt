package net.vannername.qol

import com.mojang.blaze3d.systems.RenderSystem
import me.x150.renderer.event.RenderEvents
import me.x150.renderer.render.Renderer2d
import me.x150.renderer.util.RendererUtils
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.CompassAnglePredicateProvider
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.texture.SpriteAtlasHolder
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.texture.TextureManager
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LodestoneTrackerComponent
import net.minecraft.item.CompassItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.GlobalPos
import net.minecraft.world.tick.SimpleTickScheduler
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import javax.imageio.ImageIO

object QoLModClient : ClientModInitializer {

	override fun onInitializeClient() {

		RenderEvents.HUD.register { matrices ->
			RendererUtils.registerBufferedImageTexture(Identifier("nav_compass"), ImageIO.read(File("C:/Users/the_best/Documents/BlockBench Models/nav compass/compass_00.png")))
			renderCompass(matrices.matrices, MinecraftClient.getInstance().player!!)
		//			ClientTickEvents.END_WORLD_TICK.register { world ->
//				for(p in world.players) {
//
//				}
//			}
		}

	}


	private fun renderCompass(matrixStack: MatrixStack, p: ClientPlayerEntity) {
//			RenderSystem.setShaderTexture(0, Identifier("minecraft:item/compass_00"))
//		Renderer2d.renderTexture(matrixStack, Identifier("nav_compass"), 100.0, 100.0, 16.0, 16.0)

//		p.sendMessage(Text.literal(CompassAnglePredicateProvider { world, stack, entity -> GlobalPos(world.registryKey, BlockPos(0, 50, 0)) }
//			.unclampedCall(
//				ItemStack(Items.COMPASS), p.clientWorld, p, 0
//			).toString()))

		val compass = ItemStack(Items.COMPASS)
		compass.set(DataComponentTypes.LODESTONE_TRACKER, LodestoneTrackerComponent(Optional.of(GlobalPos.create(p.world.registryKey, BlockPos(0, 50, 0))), true))
		val model = MinecraftClient.getInstance().itemRenderer.getModel(compass, null, null, 0)
		RenderSystem.setShaderTexture()
//		model.getQuads()
//		RenderSystem.getModelViewStack()
//		Renderer2d.renderTexture(matrixStack)
//		MinecraftClient.getInstance().itemRenderer.renderItem(compass, ModelTransformationMode.GUI, 15, 15, matrixStack, VertexConsumerProvider.immediate(
//			Tessellator.getInstance().buffer
//		), null, 0)
//	}
}