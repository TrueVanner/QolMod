package me.vannername.qol.main.commands.serverchest

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import me.vannername.qol.main.QoLMod
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import java.io.File

object ServerChestUtils {
    object ServerChest : SimpleInventory(27) {
        init {
            deserializeAndLoad()
            QoLMod.logger.debug("Server chest successfully deserialized.")
        }

        override fun onClose(player: PlayerEntity?) {
            serializeAndSave()
            QoLMod.logger.debug("Server chest successfully serialized.")
        }

        fun serializeAndSave() {
            val storageFile = getStorageFile()
            storageFile.writeText("") // empty the file
            for (i in 0..26) {
                getStack(i).run {
                    if (!isEmpty) {
                        try {
                            storageFile.appendText("$i|${serializeItemStack(this)}\n")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        fun deserializeAndLoad(): Map<Int, ItemStack> {
            var serverChestData: MutableMap<Int, ItemStack> = mutableMapOf()
            try {
                val data = getStorageFile().readText().takeIf { !it.isEmpty() } ?: return serverChestData
                val entries = data.split("\n")
                for (item in entries) {
                    if (!item.isEmpty()) {
                        val split = item.split("|")
                        val slot = split[0].toInt()
                        val itemStack = split[1]
                        val stack = deserializeItemStack(itemStack)
                        setStack(slot, stack)
//                        serverChestData += slot to stack
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return serverChestData
        }
    }

    private lateinit var serverChest: ServerChest

    fun loadServerChest() {
        if (!::serverChest.isInitialized) {
            serverChest = ServerChest
        }
    }

    fun getServerChest(): SimpleInventory {
        return serverChest
    }

    fun serializeItemStack(stack: ItemStack): String? {
        val json = ItemStack.CODEC.encode(stack, JsonOps.INSTANCE, JsonOps.INSTANCE.empty())
        val string = Gson().toJson(json.result().get()).takeIf { json.result().isPresent }

        return string
    }

    fun deserializeItemStack(stackData: String): ItemStack {
        var jsonDecoded = JsonParser.parseString(stackData)
        var stackDecoded = ItemStack.CODEC.decode(JsonOps.INSTANCE, jsonDecoded)
        return stackDecoded.result().get().first
    }

    fun getStorageFile(): File {
        // create a file if it doesn't exist, return regardless
        return File("config/vannername-qol-mod/serverchest.txt").apply {
            createNewFile()
        }
    }

    fun saveServerChest() {
        // fix: this caused a server error
        if (::serverChest.isInitialized) {
            serverChest.serializeAndSave()
        }
    }
}