package me.vannername.qol.main.items

import me.vannername.qol.main.QoLMod
import me.vannername.qol.main.utils.Utils
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.item.Item
import net.minecraft.item.ItemGroups
import net.minecraft.item.Items
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
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

    fun register(path: String, factory: (settings: Item.Settings) -> Item, settings: Item.Settings): Item {
        val registryKey = RegistryKey.of(RegistryKeys.ITEM, Utils.MyIdentifier(path));
        return Items.register(registryKey, factory, settings);
    }


    val SERVER_CHEST = register("server_chest", ::Item, Item.Settings().maxCount(1).rarity(Rarity.RARE).fireproof())
}