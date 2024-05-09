package net.vannername.qol

import me.x150.renderer.event.RenderEvents
import me.x150.renderer.render.Renderer2d
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LodestoneTrackerComponent
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.GlobalPos
import java.util.*

object QoLModClient : ClientModInitializer {

	override fun onInitializeClient() {

		RenderEvents.HUD.register { matrices ->
//			RendererUtils.registerBufferedImageTexture(Identifier("nav_compass"), ImageIO.read(File("C:/Users/the_best/Documents/BlockBench Models/nav compass/compass_00.png")))

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

//		Renderer2d.renderTexture(matrixStack, )

		val client = MinecraftClient.getInstance()
		val compass = ItemStack(Items.COMPASS)
		compass.set(DataComponentTypes.LODESTONE_TRACKER, LodestoneTrackerComponent(Optional.of(GlobalPos.create(p.world.registryKey, BlockPos(0, 50, 0))), true))

		try {
			val tessellator = Tessellator.getInstance()
			val builder = tessellator.buffer
			client.itemRenderer.renderItem(compass, ModelTransformationMode.GROUND, 0xF000F0, OverlayTexture.DEFAULT_UV, matrixStack, VertexConsumerProvider.immediate(builder), null, 0)
			BufferRenderer.draw(builder.end())

		} catch (e: Exception) {
			println("fuckup")
		}
//		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE)


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