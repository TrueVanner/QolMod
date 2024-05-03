package net.vannername.qol

import eu.pb4.playerdata.api.storage.JsonDataStorage
import eu.pb4.playerdata.api.storage.PlayerDataStorage
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.test.TestFunctions
import net.vannername.qol.commands.*
import net.vannername.qol.gui.MainGUI
import net.vannername.qol.schemes.PlayerData
import net.vannername.qol.utils.PlayerUtils
import net.vannername.qol.utils.PlayerUtils.displayActionbarCoords
import net.vannername.qol.utils.Utils.getPlayerData
import org.slf4j.Logger
import org.slf4j.LoggerFactory


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
//		Testing()
		MainGUI()



		ServerTickEvents.END_WORLD_TICK.register { world ->
			for(p in world.players) {
				p.displayActionbarCoords()
			}
		}
	}
}