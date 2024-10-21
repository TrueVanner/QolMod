package me.vannername.qol.main.commands.serverchest

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import me.vannername.qol.main.commands.util.ServerCommandHandlerBase
import me.vannername.qol.main.items.ModItems
import me.vannername.qol.main.utils.Utils.sendCommandError
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.SimpleNamedScreenHandlerFactory
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text


object ServerChest : ServerCommandHandlerBase("serverchest", listOf("svc")) {
    override fun init() {
        setDefaultAction(::run)
        super.init()
    }

    @Throws(CommandSyntaxException::class)
    override fun run(ctx: CommandContext<ServerCommandSource>): Int {
        val p = ctx.source.playerOrThrow

        if (!p.inventory.containsAny { stack -> stack.isOf(ModItems.SERVER_CHEST) }) {
            return ctx.sendCommandError("You don't have a server chest in your inventory!")
        }

        openServerChest(p)
        return 1
    }

    private fun openServerChest(player: ServerPlayerEntity) {
        ServerChestUtils.loadServerChest()
        player.openHandledScreen(SimpleNamedScreenHandlerFactory({ syncId: Int, inventory: PlayerInventory?, _: PlayerEntity? ->
            GenericContainerScreenHandler.createGeneric9x3(
                syncId,
                inventory,
                ServerChestUtils.getServerChest()
            )
        }, Text.translatable("Server Chest")))
    }
}