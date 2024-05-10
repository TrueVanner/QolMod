package net.vannername.qol

import com.mojang.blaze3d.systems.RenderSystem
import me.x150.renderer.event.RenderEvents
import me.x150.renderer.render.MSAAFramebuffer
import me.x150.renderer.render.Renderer2d
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarted
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.render.*
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.session.telemetry.WorldLoadedEvent
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LodestoneTrackerComponent
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.GlobalPos
import org.lwjgl.BufferUtils
import java.util.*

object QoLModClient : ClientModInitializer {

	override fun onInitializeClient() {

		ServerLifecycleEvents.SERVER_STARTED.register { server ->

			val client = MinecraftClient.getInstance()
			val compass = ItemStack(Items.COMPASS)
			client.textureManager.getTexture(Identifier("renderer", "icon.png"))
			println(client.textureManager.getTexture(Identifier("compass_00")))
		}

		RenderEvents.HUD.register { matrices ->

		}

//		RenderEvents.HUD.register { matrices ->
////			RendererUtils.registerBufferedImageTexture(Identifier("nav_compass"), ImageIO.read(File("C:/Users/the_best/Documents/BlockBench Models/nav compass/compass_00.png")))
//
//			renderCompass(matrices.matrices, MinecraftClient.getInstance().player!!)
//		//			ClientTickEvents.END_WORLD_TICK.register { world ->
////				for(p in world.players) {
////
////				}
////			}
//		}

	}


	private fun renderCompass(matrixStack: MatrixStack, p: ClientPlayerEntity) {
//			RenderSystem.setShaderTexture(0, Identifier("minecraft:item/compass_00"))
//		Renderer2d.renderTexture(matrixStack, Identifier("nav_compass"), 100.0, 100.0, 16.0, 16.0)

		Renderer2d.renderTexture(matrixStack, Identifier("compass"), 100.0, 100.0, 16.0, 16.0)

//		p.sendMessage(Text.literal(CompassAnglePredicateProvider { world, stack, entity -> GlobalPos(world.registryKey, BlockPos(0, 50, 0)) }
//			.unclampedCall(
//				ItemStack(Items.COMPASS), p.clientWorld, p, 0
//			).toString()))


		val client = MinecraftClient.getInstance()
		val compass = ItemStack(Items.COMPASS)
		compass.set(DataComponentTypes.LODESTONE_TRACKER, LodestoneTrackerComponent(Optional.of(GlobalPos.create(p.world.registryKey, BlockPos(0, 50, 0))), true))
		client.textureManager.getTexture(Identifier("item", "compass.json"))
		client.textureManager.getTexture(Identifier("compass_00"))

	//		println(compass.registryEntry.key)
//		try {
//			val tessellator = Tessellator.getInstance()
//			val builder = tessellator.buffer
//
////			client.itemRenderer.renderItem(compass, ModelTransformationMode.GROUND, 0xF000F0, OverlayTexture.DEFAULT_UV, matrixStack, VertexConsumerProvider.immediate(builder), null, 0)
////			client.itemRenderer.renderItem(ItemStack(Items.DIRT), ModelTransformationMode.GROUND, 0xF000F0, OverlayTexture.DEFAULT_UV, matrixStack, VertexConsumerProvider.immediate(builder), null, 0)
////			RenderSystem.setShader { GameRenderer.getPositionTexProgram() }
//			tessellator.draw()
//		} catch (e: Exception) {
//			println("fuckup")
//		}


//		for(quad in MinecraftClient.getInstance().itemRenderer.getModel(compass, null, null, 0).getQuads(null, null, Random.create())) {
//			buffer.quad(matrixStack.peek(), quad, )
//		}

//		tessellator.draw()

	//		val model = MinecraftClient.getInstance().itemRenderer.getModel(compass, null, null, 0).getQuads(null, null, Random.create())
//		val matrix: Matrix4f = Matrix4f(FloatBuffer.wrap(model[0].vertexData.map { elem -> elem.toFloat() }.toFloatArray()))

//		model.getQuads()
//		RenderSystem.getModelViewStack()
//		Renderer2d.renderTexture(matrixStack)
//		MinecraftClient.getInstance().itemRenderer.renderItem(compass, ModelTransformationMode.GUI, 15, 15, matrixStack, VertexConsumerProvider.immediate(
//			Tessellator.getInstance().buffer
//		), null, 0)
	}
}