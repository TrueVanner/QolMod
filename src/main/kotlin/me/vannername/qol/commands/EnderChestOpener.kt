package me.vannername.qol.commands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import me.vannername.qol.utils.Utils.sendCommandError
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Items
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.SimpleNamedScreenHandlerFactory
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text


class EnderChestOpener {
    init {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                CommandManager.literal("e")
                    .executes { ctx -> run(ctx) }
            )
        }
    }

    @Throws(CommandSyntaxException::class)
    private fun run(ctx: CommandContext<ServerCommandSource>): Int {
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