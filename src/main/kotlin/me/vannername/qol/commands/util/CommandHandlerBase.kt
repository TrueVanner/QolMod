package me.vannername.qol.commands.util

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import me.vannername.qol.QoLMod
import me.vannername.qol.commands.util.CommandHandlerBase.CommandNodeKey
import me.vannername.qol.utils.Utils
import me.vannername.qol.utils.Utils.appendCommandSuggestion
import me.vannername.qol.utils.Utils.sendMessage
import me.vannername.qol.utils.Utils.sendSimpleMessage
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandSource
import net.minecraft.command.suggestion.SuggestionProviders
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.concurrent.CompletableFuture

/**
 * A class that provides a template for command handlers.
 *
 * If nothing is specified, running init() for the subclass
 * of this template will register an empty command that throws an error
 * when called.
 *
 * CommandHandlerTemplate contains a basic structure for a command handler.
 *
 * @param commandName the name of the handled command. It's later used
 * for registering the command.
 * @param S the command source used by the command handler.
 * @param side the side on which the command is registered.
 */

abstract class CommandHandlerBase<S : CommandSource>(val commandName: String, val side: EnvType) {

    // stores the suggestion providers used for this command.
    private var suggestionProviders: Map<SuggestionProviderKey,
            SuggestionProvider<S>> = mutableMapOf()

    // stores the command nodes (before building) that make up the command.
    private var commandNodes: Map<CommandNodeKey,
            CommandNode<S>> = mutableMapOf()

    // The root node. Must be defined during compile time.
    private lateinit var rootNode: CommandNode<S>
    private var defaultAction: Command<S>? = null

    // for storing descriptions of specific paths in the command tree.
    private var pathDescriptions: Map<List<CommandNodeKey>, String> = mutableMapOf()

    // to store whether a node is considered to be required or not
    private var requiredNodes: List<CommandNodeKey> = mutableListOf()

    protected fun setDefaultAction(action: Command<S>) {
        this.defaultAction = action
    }

    /**
     * A way to store the keys for the suggestion providers.
     */
    protected interface SuggestionProviderKey {
        fun key(): String
    }

    protected fun SuggestionProviderKey.getProvider(): SuggestionProvider<S> {
        // TODO: figure out why this throws an exception
        return suggestionProviders[this]!!
    }

    protected fun SuggestionProviderKey.getSuggestions(
        ctx: CommandContext<S>,
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
    protected fun CommandNodeKey.getNode(): CommandNode<S> {
        return commandNodes[this]!!
    }

    protected fun CommandNodeKey.isRequired(): Boolean {
        return requiredNodes.contains(this)
    }

    /**
     * Provides easy access to the globally defined root key.
     * Keeping a single root key helps remove ambiguity and the need
     * to define a root key for each command.
     */
    protected val ROOT = CommandNodeKey { "ROOT" }

    // a node key for registering the help command.
    protected val HELP = CommandNodeKey { "HELP" }

    /**
     * Records the suggestion provider.
     *
     * @return associated key.
     */
    protected fun registerSuggestionProvider(
        key: SuggestionProviderKey,
        provider: SuggestionProvider<S>
    ) {
        suggestionProviders += key to SuggestionProviders.register(
            Utils.MyIdentifier("suggestionprovider-$commandName-${key.key().lowercase()}"),
            provider as SuggestionProvider<CommandSource>?
        )
//        return key
    }

    /**
     * Records and builds the command node.
     * If the name of the key is ROOT, marks the node as root automatically.
     */
    protected fun ArgumentBuilder<*, *>.register(key: CommandNodeKey, required: Boolean = true) {
        val node = this.build() as CommandNode<S>
        commandNodes += key to node
        if (key == ROOT) {
            rootNode = node
        }
        if (node is ArgumentCommandNode<*, *> && required) {
            requiredNodes += key
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
        when (side) {
            EnvType.CLIENT -> {
                ClientCommandManager
                    .literal(commandName)
                    .executes((defaultAction ?: Command { ctx -> help(ctx) }) as Command<FabricClientCommandSource>?)
                    .register(ROOT)

                ClientCommandManager
                    .literal("help")
                    .executes(::help)
                    .register(HELP)
            }

            EnvType.SERVER -> {
                CommandManager
                    .literal(commandName)
                    .executes((defaultAction ?: Command { ctx -> help(ctx) }) as Command<ServerCommandSource>?)
                    .register(ROOT)

                CommandManager
                    .literal("help")
                    .executes(::help)
                    .register(HELP)
            }
        }
    }

    /**
     * Can be used to define the structure of the Brigadier tree,
     * if there are several nodes.
     */
    protected open fun commandStructure() {
        try {
            ROOT.addChild(HELP)
        } catch (e: NullPointerException) {
            QoLMod.logger.warn("Warning: help node not defined for command /${commandName}")
        }
    }

    /**
     * Registers the command.
     *
     * Note: registerSuggestionProviders() and registerCommandNodes() must be called!
     */
    protected open fun register() {
        when (side) {
            EnvType.CLIENT -> {
                ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
                    dispatcher.root.addChild(rootNode as CommandNode<FabricClientCommandSource>)
                }
            }

            EnvType.SERVER -> {
                CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
                    dispatcher.root.addChild(rootNode as CommandNode<ServerCommandSource>)
                }
            }
        }
    }

    /**
     * By default, should be called in the main class to load the functionality
     * associated with each command. If necessary, can be extended.
     *
     * Note: defaultAction must be set before this function is called.
     */
    open fun init() {
        registerSuggestionProviders()
        registerCommandNodes()
        commandStructure()
        defineHelpMessages()
        register()
    }

    protected open fun defineHelpMessages() {
        addPathDescriptions(
            listOf(HELP) to "Shows the help message for this command."
        )
    }

    private fun validatePath(path: List<CommandNodeKey>): Boolean {
        var parent = path.first()
        for (node in path.drop(1)) {
            if (!parent.getNode().children.contains(node.getNode())) return false
            parent = node
        }
        return true
    }

    /**
     * Adds descriptions for specific paths in the command tree.
     * Validates the specified path.
     *
     * @param appendRoot whether the ROOT node must automatically be set as the starting point
     * for the path. Defaults to true
     * @param descriptions a list of pairs, where the first element is the path and the second
     * is its description.
     */
    protected fun addPathDescriptions(
        vararg descriptions: Pair<List<CommandNodeKey>, String>,
        appendRoot: Boolean = true
    ) {
        for (descr in descriptions) {
            val finalList = if (descr.first.first() == ROOT || !appendRoot) descr.first else listOf(ROOT) + descr.first
            if (!validatePath(finalList)) {
                throw IllegalArgumentException("The command path ${pathToString(finalList)} does not exist.")
            }
            pathDescriptions += finalList to descr.second
        }
    }

    private fun pathToString(path: List<CommandNodeKey>): String {
        var toReturn = "/"
        for (node in path) {
            toReturn += when {
                node.getNode() is LiteralCommandNode<S> -> node.getNode().name
                else -> {
                    when {
                        node.isRequired() -> "<${node.getNode().name}>"
                        else -> "[<${node.getNode().name}>]"
                    }
                }
            } + " "
        }
        return toReturn.trim()
    }

    /**
     * Can be used to send the help message to the player.
     */
    @Throws(CommandSyntaxException::class)
    protected open fun help(ctx: CommandContext<*>): Int {
        ctx.sendSimpleMessage("HELP MESSAGE FOR /$commandName", Formatting.YELLOW)
        for (descr in pathDescriptions) {
            val path = pathToString(descr.key)
            ctx.sendMessage(
                Text.literal("")
                    .appendCommandSuggestion(path)
                    .append(Text.literal(" - ${descr.value}"))
                    .formatted(Formatting.YELLOW)
            )

//            ctx.simpleMessage("${} - ${descr.value}", Formatting.YELLOW)
        }
        return 1
    }

    /**
     * Can be used to contain the main code for the command.
     */
    @Throws(CommandSyntaxException::class)
    protected open fun run(ctx: CommandContext<S>): Int {
        throw UnsupportedOperationException("The main code for this command has not been implemented.")
    }
}
