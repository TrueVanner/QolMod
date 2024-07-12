package me.vannername.qol.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.CommandNode
import me.vannername.qol.commands.CommandHandlerTemplate.CommandNodeKey
import me.vannername.qol.utils.Utils
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandSource
import net.minecraft.command.suggestion.SuggestionProviders
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture

/**
 * A class that provides a template for command handlers.
 *
 * If nothing is specified, running init() for the subclass
 * of this template will register an empty command that throws an error
 * when called.
 *
 * To specify behavior of the command, the following can be implemented:
 * - help() to show the help message to the player when command is called
 * without arguments
 *
 * - run() to contain the main code for the command. You can also set
 * the function as default using setDefaultAction()
 *
 * - Enums implementing SuggestionProviderKey and CommandNodeKey
 *
 * - registerSuggestionProviders() to record suggestion providers using
 * registerSuggestionProvider()
 *
 * - registerCommandNodes() to record command nodes using .register()
 * extension on the ArgumentBuilder
 *
 * - commandStructure() to define the structure of the Brigadier tree
 * (for commands that have more than 1 node)
 *
 * @param commandName the name of the handled command. It's later used
 * for registering the command.
 */
abstract class CommandHandlerTemplate(val commandName: String) {

    // stores the suggestion providers used for this command.
    private var suggestionProviders: Map<SuggestionProviderKey,
            SuggestionProvider<ServerCommandSource>> = mutableMapOf()

    // stores the command nodes (before building) that make up the command.
    private var commandNodes: Map<CommandNodeKey,
            CommandNode<ServerCommandSource>> = mutableMapOf()

    // The root node. Must be defined during compile time.
    private lateinit var rootNode: CommandNode<ServerCommandSource>
    private var defaultAction: Command<ServerCommandSource>? = null

    protected fun setDefaultAction(action: Command<ServerCommandSource>) {
        this.defaultAction = action
    }

    /**
     * A way to store the keys for the suggestion providers.
     */
    protected interface SuggestionProviderKey {
        fun key(): Identifier
    }

    protected fun SuggestionProviderKey.getSuggestions(
        ctx: CommandContext<ServerCommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return suggestionProviders[this]!!.getSuggestions(ctx, builder)
    }

    /**
     * Same as SuggestionsProviderKey, but for command nodes.
     */
    protected fun interface CommandNodeKey {
        fun key(): String
    }

    /**
     * Adds a child to the node associated with this key.
     */
    protected fun CommandNodeKey.addChild(childKey: CommandNodeKey) {
        commandNodes[this]!!.addChild(commandNodes[childKey]!!)
    }

    /**
     * Performs the addChild() on several nodes.
     */
    protected fun CommandNodeKey.addChildren(vararg childKeys: CommandNodeKey) {
        for (key in childKeys) {
            this.addChild(key)
        }
    }

    /**
     * Returns the node associated with this key.
     */
    protected fun CommandNodeKey.getNode(): CommandNode<ServerCommandSource> {
        return commandNodes[this]!!
    }

    /**
     * This is an example of the storage for suggestion provider keys.
     * Implement own extension of SuggestionProviderKey in the command.
     */
    private enum class BaseSuggestionProviderKeys : SuggestionProviderKey {
        EXAMPLE_PROVIDER;

        override fun key(): Identifier = Utils.MyIdentifier(this.name)
    }

    protected enum class BaseCommandNodeKeys : CommandNodeKey {
        EXAMPLE_KEY;

        override fun key(): String = this.name
    }

    /**
     * Provides easy access to the globally defined root key.
     * Keeping a single root key helps remove ambiguity and the need
     * to define a root key for each command.
     */
    protected val ROOT = CommandNodeKey { "ROOT" }

    /**
     * Records the suggestion provider.
     *
     * @return associated key.
     */
    protected fun registerSuggestionProvider(
        key: SuggestionProviderKey,
        provider: SuggestionProvider<CommandSource>
    ) {
        suggestionProviders += key to SuggestionProviders.register(key.key(), provider)
//        return key
    }

    /**
     * Records and builds the command node.
     * If the name of the key is ROOT, marks the node as root automatically.
     */
    protected fun ArgumentBuilder<ServerCommandSource, *>.register(key: CommandNodeKey) {
        val node = this.build()
        commandNodes += key to node
        if (key == ROOT) {
            rootNode = node
        }
    }

    /**
     * If necessary, use to register suggestion providers.
     */
    protected open fun registerSuggestionProviders() {}

    /**
     * Can be used to register the command nodes used in the final command.
     * By default, registers the root node, which is a literal with the same
     * name as the specified command name and executes either the default action
     * or shows the help message to the user.
     */
    protected open fun registerCommandNodes() {
        CommandManager
            .literal(commandName)
            .executes(defaultAction ?: Command { ctx -> help(ctx) })
            .register(ROOT)
    }

    /**
     * Can be used to define the structure of the Brigadier tree,
     * if there are several nodes.
     */
    protected open fun commandStructure() {}

    /**
     * Registers the command.
     *
     * Note: registerSuggestionProviders() and registerCommandNodes() must be called!
     */
    protected open fun register() {
        registerSuggestionProviders()
        registerCommandNodes()

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.root.addChild(rootNode)
            commandStructure()
        }
    }

    /**
     * By default, should be called in the main class to load the functionality
     * associated with each command. If necessary, can be extended.
     *
     * Note: defaultAction must be set before this function is called.
     */
    open fun init() = register()

    /**
     * Can be used to send the help message to the player.
     */
    @Throws(CommandSyntaxException::class)
    protected open fun help(ctx: CommandContext<ServerCommandSource>): Int {
        throw UnsupportedOperationException("The main code for this command has not been implemented.")
    }

    /**
     * Can be used to contain the main code for the command.
     */
    @Throws(CommandSyntaxException::class)
    protected open fun run(ctx: CommandContext<ServerCommandSource>): Int {
        throw UnsupportedOperationException("The main code for this command has not been implemented.")
    }
}