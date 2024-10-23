package me.vannername.qol.main.items

import me.vannername.qol.main.QoLMod
import me.vannername.qol.main.utils.Utils
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.item.Item
import net.minecraft.item.ItemGroups
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Rarity

object ModItems {
    fun init() {
        fun registerItemsToGroups() {
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
                .register { itemGroup -> itemGroup.add(SERVER_CHEST) }
        }

        QoLMod.logger.info("Registering items...")
        registerItemsToGroups()
    }

    fun register(item: Item, id: String): Item {
        val itemID = Utils.MyIdentifier(id)
        return Registry.register(Registries.ITEM, itemID, item)
    }

    val SERVER_CHEST = register(Item(Item.Settings().maxCount(1).rarity(Rarity.RARE).fireproof()), "server_chest")
}