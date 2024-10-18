package me.vannername.qol.main.commands.serverchest

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import java.io.File

object ServerChestUtils {
    private lateinit var serverChest: SimpleInventory

    fun loadServerChest() {
        if (!::serverChest.isInitialized) {
            val serverChestData: Map<Int, ItemStack> = deserializeServerChest()
            serverChest = SimpleInventory(27)
            for ((slot, item) in serverChestData) {
                serverChest.setStack(slot, item)
            }
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

    fun deserializeServerChest(): Map<Int, ItemStack> {
        var serverChestData: Map<Int, ItemStack> = mutableMapOf()
        try {
            val data = getStorageFile().readText().takeIf { !it.isEmpty() } ?: return serverChestData
            val entries = data.split("\n")
            for (item in entries) {
                if (!item.isEmpty()) {
                    val split = item.split("|")
                    val slot = split[0].toInt()
                    val itemStack = split[1]
                    val stack = deserializeItemStack(itemStack)
                    serverChestData += slot to stack
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return serverChestData
    }

    fun getStorageFile(): File {
        // create a file if it doesn't exist, return regardless
        return File("config/vannername-qol-mod/serverchest.txt").apply {
            createNewFile()
        }
    }

    fun serializeServerChest() {
        val storageFile = getStorageFile()
        storageFile.writeText("") // empty the file
        for (i in 0..26) {
            serverChest.getStack(i).run {
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
}