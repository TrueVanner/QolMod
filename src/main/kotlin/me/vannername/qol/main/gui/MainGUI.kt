package me.vannername.qol.main.gui

/*
import com.mojang.authlib.GameProfile
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.elements.*
import eu.pb4.sgui.api.gui.SimpleGui
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.*

class MainGUI {
    init {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ -> register(dispatcher) }
    }

    private fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("gui")
                .executes(::perform)
        )
    }

    private fun perform(context: CommandContext<ServerCommandSource>): Int {
        try {
            val player = context.source.player
            val gui: SimpleGui = object : SimpleGui(ScreenHandlerType.GENERIC_3X3, player, false) {
                override fun onClick(
                    index: Int,
                    type: ClickType,
                    action: SlotActionType,
                    element: GuiElementInterface
                ): Boolean {
                    this.player.sendMessage(Text.literal(type.toString()), false)

                    return super.onClick(index, type, action, element)
                }

                override fun onTick() {
                    this.setSlot(0, GuiElementBuilder(Items.ARROW).setCount((player!!.serverWorld.time % 127).toInt()))
                    super.onTick()
                }

                override fun canPlayerClose(): Boolean {
                    return false
                }
            }

            gui.title = Text.literal("Nice")
            gui.setSlot(0, GuiElementBuilder(Items.ARROW).setCount(100))
            gui.setSlot(1, AnimatedGuiElement(
                arrayOf(
                    Items.NETHERITE_PICKAXE.defaultStack,
                    Items.DIAMOND_PICKAXE.defaultStack,
                    Items.GOLDEN_PICKAXE.defaultStack,
                    Items.IRON_PICKAXE.defaultStack,
                    Items.STONE_PICKAXE.defaultStack,
                    Items.WOODEN_PICKAXE.defaultStack
                ), 10, false
            ) { _: Int, _: ClickType?, _: SlotActionType? -> })

            gui.setSlot(
                2, AnimatedGuiElementBuilder()
                    .setItem(Items.NETHERITE_AXE).setDamage(150).saveItemStack()
                    .setItem(Items.DIAMOND_AXE).setDamage(150).unbreakable().saveItemStack()
                    .setItem(Items.GOLDEN_AXE).glow().saveItemStack()
//                    .setItem(Items.IRON_AXE).enchant(Enchantments.AQUA_AFFINITY, 1).
                    .setItem(Items.STONE_AXE).saveItemStack()
                    .setItem(Items.WOODEN_AXE).saveItemStack()
                    .setInterval(10).setRandom(true)
            )

            for (x in 3 until gui.size) {
                val itemStack = Items.STONE.defaultStack
                itemStack.count = x
                gui.setSlot(x, GuiElement(
                    itemStack
                ) { _: Int, _: ClickType?, _: SlotActionType? -> })
            }

            gui.setSlot(
                5, GuiElementBuilder(Items.PLAYER_HEAD)
                    .setSkullOwner(
                        "ewogICJ0aW1lc3RhbXAiIDogMTYxOTk3MDIyMjQzOCwKICAicHJvZmlsZUlkIiA6ICI2OTBkMDM2OGM2NTE0OGM5ODZjMzEwN2FjMmRjNjFlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ5emZyXzciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDI0OGVhYTQxNGNjZjA1NmJhOTY5ZTdkODAxZmI2YTkyNzhkMGZlYWUxOGUyMTczNTZjYzhhOTQ2NTY0MzU1ZiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                        null, null
                    )
                    .setName(Text.literal("Battery"))
                    .glow()
            )

            gui.setSlot(
                6, GuiElementBuilder(Items.PLAYER_HEAD)
                    .setSkullOwner(
                        GameProfile(UUID.fromString("f5a216d9-d660-4996-8d0f-d49053677676"), "patbox"),
                        player!!.server
                    )
                    .setName(Text.literal("Patbox's Head"))
                    .glow()
            )

            gui.setSlot(7, GuiElementBuilder()
                .setItem(Items.BARRIER)
                .glow()
                .setName(
                    Text.literal("Bye")
                        .setStyle(Style.EMPTY.withItalic(false).withBold(true))
                )
                .addLoreLine(Text.literal("Some lore"))
                .addLoreLine(Text.literal("More lore").formatted(Formatting.RED))
                .setCount(3)
                .setCallback { index: Int, clickType: ClickType?, actionType: SlotActionType? -> gui.close() }
            )

            gui.setSlot(8, GuiElementBuilder()
                .setItem(Items.TNT)
                .glow()
                .setName(
                    Text.literal("Test :)")
                        .setStyle(Style.EMPTY.withItalic(false).withBold(true))
                )
                .addLoreLine(Text.literal("Some lore"))
                .addLoreLine(Text.literal("More lore").formatted(Formatting.RED))
                .setCount(1)
                .setCallback { index: Int, clickType: ClickType, actionType: SlotActionType? ->
                    player.sendMessage(Text.literal("derg "), false)
                    val item = gui.getSlot(index)!!.itemStack
                    if (clickType == ClickType.MOUSE_LEFT) {
                        item.count = if (item.count == 1) item.count else item.count - 1
                    } else if (clickType == ClickType.MOUSE_RIGHT) {
                        item.count += 1
                    }
                    (gui.getSlot(index) as GuiElement?)!!.itemStack = item
                    if (item.count <= player.enderChestInventory.size()) {
                        gui.setSlotRedirect(
                            4,
                            Slot(player.enderChestInventory, item.count - 1, 0, 0)
                        )
                    }
                }
            )
            gui.setSlotRedirect(4, Slot(player.enderChestInventory, 0, 0, 0))

            gui.open()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }
}*/
