package me.vannername.qol.commands.util

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.fabricmc.api.EnvType
import net.minecraft.server.command.ServerCommandSource

/**
 * Template for a command handler.
 */
object CommandHandlerTemplate : CommandHandlerBase<ServerCommandSource>("", EnvType.SERVER) {
//object CommandHandlerTemplate : CommandHandlerBase<FabricClientCommandSource>("", EnvType.CLIENT) {

    private enum class BaseSuggestionProviderKeys : SuggestionProviderKey {
        EXAMPLEPROVIDER; // can't contain _ !

        override fun key(): String = this.name
    }

    private enum class BaseCommandNodeKeys : CommandNodeKey {
        EXAMPLE_KEY;

        override fun key(): String = this.name
    }

    override fun registerSuggestionProviders() {
//        registerSuggestionProvider(<suggestionProviderKey>)
//        { ctx, builder ->
//            CommandSource.suggestMatching(
//                <candidates (list)>, builder
//            )
//        }
    }

    override fun defineHelpMessages() {
        super.defineHelpMessages()
        addPathDescriptions(
            // listOf(<key>) to "description",
            // ...
        )
    }

    @Throws(CommandSyntaxException::class)
    override fun run(ctx: CommandContext<ServerCommandSource>): Int {
//    override fun run(ctx: CommandContext<FabricClientCommandSource>): Int {
        // val p = ctx.source.playerOrThrow

        return 1
    }
}