package me.vannername.qol.main.commands.serverchest

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import me.vannername.qol.main.commands.util.ServerCommandHandlerBase
import me.vannername.qol.main.items.ModItems
import me.vannername.qol.main.utils.Utils.appendCommandSuggestion
import me.vannername.qol.main.utils.Utils.sendMessage
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.SimpleNamedScreenHandlerFactory
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting


object ServerChestCmd : ServerCommandHandlerBase("serverchest", listOf("svc")) {
    override fun init() {
        setDefaultAction(::run)
        super.init()
    }

    override fun defineHelpMessages() {
        super.defineHelpMessages()
        addPathDescriptions(
            listOf(ROOT) to "Opens the Server Chest - a server-wide shared inventory (not available in single player!) that can be interacted by all players simultaneously. Its contents persist after a server restart. Can be crafted with gold ingots (g), diamonds (D) and an ender chest (E) in the following formation: \ngDg\nDED\ngDg.\nNote: Server Chest is also considered to be an ender chest when using /e."
        )
    }

    @Throws(CommandSyntaxException::class)
    override fun run(ctx: CommandContext<ServerCommandSource>): Int {
        val p = ctx.source.playerOrThrow

        if (!p.inventory.containsAny { stack -> stack.isOf(ModItems.SERVER_CHEST) }) {
            ctx.sendMessage(
                Text.literal("You don't have a Server Chest in your inventory! For the crafting recipe, check ")
                    .appendCommandSuggestion("/svc help").formatted(Formatting.YELLOW)
            )
            return 0
        }

//        if(QoLMod.getServer().isSingleplayer) {
//            return ctx.sendCommandError("Server Chest is not available in single player!")
//        }

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