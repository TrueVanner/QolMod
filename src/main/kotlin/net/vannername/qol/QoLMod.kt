package net.vannername.qol

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.vannername.qol.commands.*
import net.vannername.qol.gui.MainGUI
import net.vannername.qol.utils.PlayerConfig
import net.vannername.qol.utils.PlayerUtils.displayActionbarCoords
import net.vannername.qol.utils.PlayerUtils.displayNavCoords
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID


object QoLMod : ModInitializer {
	const val MOD_ID = "vannername-qol-mod"
	@JvmField
	val logger: Logger = LoggerFactory.getLogger(MOD_ID)
	@JvmField
	val playerConfigs: Map<UUID, PlayerConfig> = mutableMapOf()

	override fun onInitialize() {
//		MidnightConfig.init(MOD_ID, MidnightConfigExample::class.java)
		EnderChestOpener()
		ConfigureProperty()
		Testing()
		MainGUI()
		Navigate()

		ServerTickEvents.END_WORLD_TICK.register { world ->
			for (p in world.players) {
				p.displayActionbarCoords()
				p.displayNavCoords()
			}
		}
	}
}