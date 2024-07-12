package me.vannername.qol.commands

import com.mojang.brigadier.context.CommandContext
import me.vannername.qol.utils.Utils.sendCommandError
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Items
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.SimpleNamedScreenHandlerFactory
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text


object EnderChestOpener : CommandHandlerTemplate("e") {

    override fun init() {
        setDefaultAction(::run)
        super.init()
    }

    override fun run(ctx: CommandContext<ServerCommandSource>): Int {
        val p = ctx.source.playerOrThrow

        if (!p.inventory.containsAny { stack -> stack.isOf(Items.ENDER_CHEST) }) {
            return ctx.sendCommandError("You don't have an ender chest in your inventory!")
        }

        openEnderChest(p)
        return 1
    }

    private fun openEnderChest(player: ServerPlayerEntity) {
        player.openHandledScreen(SimpleNamedScreenHandlerFactory({ syncId: Int, inventory: PlayerInventory?, _: PlayerEntity? ->
            GenericContainerScreenHandler.createGeneric9x3(
                syncId,
                inventory,
                player.enderChestInventory
            )
        }, Text.translatable("container.enderchest")))
    }
}