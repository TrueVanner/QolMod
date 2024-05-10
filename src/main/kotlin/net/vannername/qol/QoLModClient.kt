package net.vannername.qol

import me.x150.renderer.event.RenderEvents
import me.x150.renderer.render.Renderer2d
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.CompassAnglePredicateProvider
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.GlobalPos

object QoLModClient : ClientModInitializer {

	override fun onInitializeClient() {
		val angleProvider = CompassAnglePredicateProvider { world, _, _ -> GlobalPos(world.registryKey, BlockPos(0, 50, 0)) }
		RenderEvents.HUD.register { matrices ->
			renderCompass(matrices.matrices, angleProvider, MinecraftClient.getInstance().player!!)
		}

	}


	private fun renderCompass(matrixStack: MatrixStack, angleProvider: CompassAnglePredicateProvider, p: ClientPlayerEntity) {
		val angle = (angleProvider.unclampedCall(
			ItemStack(Items.COMPASS), p.clientWorld, p, 0
		))

		val textureID: String = when {
			angle == 0f -> "16"
			angle < 0.015625f -> "17"
			angle < 0.046875f -> "18"
			angle < 0.078125f -> "19"
			angle < 0.109375f -> "20"
			angle < 0.140625f -> "21"
			angle < 0.171875f -> "22"
			angle < 0.203125f -> "23"
			angle < 0.234375f -> "24"
			angle < 0.265625f -> "25"
			angle < 0.296875f -> "26"
			angle < 0.328125f -> "27"
			angle < 0.359375f -> "28"
			angle < 0.390625f -> "29"
			angle < 0.421875f -> "30"
			angle < 0.453125f -> "31"
			angle < 0.484375f -> "00"
			angle < 0.515625f -> "01"
			angle < 0.546875f -> "02"
			angle < 0.578125f -> "03"
			angle < 0.609375f -> "04"
			angle < 0.640625f -> "05"
			angle < 0.671875f -> "06"
			angle < 0.703125f -> "07"
			angle < 0.734375f -> "08"
			angle < 0.765625f -> "09"
			angle < 0.796875f -> "10"
			angle < 0.828125f -> "11"
			angle < 0.859375f -> "12"
			angle < 0.890625f -> "13"
			angle < 0.921875f -> "14"
			angle < 0.953125f -> "15"
			else -> "16"
		}

		Renderer2d.renderTexture(matrixStack, Identifier("textures/item/compass_${textureID}.png"), 100.0, 100.0, 16.0, 16.0)



//		val client = MinecraftClient.getInstance()
//		val compass = ItemStack(Items.COMPASS)
//		compass.set(DataComponentTypes.LODESTONE_TRACKER, LodestoneTrackerComponent(Optional.of(GlobalPos.create(p.world.registryKey, BlockPos(0, 50, 0))), true))
//		client.textureManager.getTexture(Identifier("item", "compass.json"))
//		client.textureManager.getTexture(Identifier("compass_00"))

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