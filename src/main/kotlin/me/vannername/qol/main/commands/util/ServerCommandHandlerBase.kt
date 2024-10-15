package me.vannername.qol.main.commands.util

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
import me.vannername.qol.main.utils.Utils
import me.vannername.qol.main.utils.Utils.appendCommandSuggestion
import me.vannername.qol.main.utils.Utils.sendMessage
import me.vannername.qol.main.utils.Utils.sendSimpleMessage
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandSource
import net.minecraft.command.suggestion.SuggestionProviders
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.concurrent.CompletableFuture
import kotlin.collections.drop
import kotlin.collections.first
import kotlin.collections.plus
import kotlin.jvm.Throws
import kotlin.text.lowercase
import kotlin.text.trim
import kotlin.to

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

abstract class ServerCommandHandlerBase(val commandName: String) {

    // stores the suggestion providers used for this command.
    private var suggestionProviders: Map<SuggestionProviderKey,
            SuggestionProvider<ServerCommandSource>> = mutableMapOf()

    // stores the command nodes (before building) that make up the command.
    private var commandNodes: Map<CommandNodeKey,
            CommandNode<ServerCommandSource>> = mutableMapOf()

    // The root node. Must be defined during compile time.
    private lateinit var rootNode: CommandNode<ServerCommandSource>
    private var defaultAction: Command<ServerCommandSource>? = null

    // for storing descriptions of specific paths in the command tree.
    private var pathDescriptions: Map<List<CommandNodeKey>, String> = mutableMapOf()

    // to store whether a node is considered to be required or not
    private var requiredNodes: List<CommandNodeKey> = mutableListOf()

    protected fun setDefaultAction(action: Command<ServerCommandSource>) {
        this.defaultAction = action
    }

    /**
     * A way to store the keys for the suggestion providers.
     */
    protected interface SuggestionProviderKey {
        fun key(): String
    }

    protected fun SuggestionProviderKey.getProvider(): SuggestionProvider<ServerCommandSource> {
        // TODO: figure out why this throws an exception
        return suggestionProviders[this]!!
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
        provider: SuggestionProvider<ServerCommandSource>
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
    protected fun ArgumentBuilder<ServerCommandSource, *>.register(key: CommandNodeKey, required: Boolean = true) {
        val node = this.build()
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
        CommandManager
            .literal(commandName)
            .executes((defaultAction ?: Command { ctx -> help(ctx) }))
            .register(ROOT)

        CommandManager
            .literal("help")
            .executes(::help)
            .register(HELP)
    }

    /**
     * Can be used to define the structure of the Brigadier tree,
     * if there are several nodes.
     */
    protected open fun commandStructure() {
        ROOT.addChild(HELP)
    }

    /**
     * Registers the command.
     *
     * Note: registerSuggestionProviders() and registerCommandNodes() must be called!
     */
    protected open fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.root.addChild(rootNode)
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
            toReturn + when {
                node.getNode() is LiteralCommandNode<ServerCommandSource> -> node.getNode().name
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
    protected open fun help(ctx: CommandContext<ServerCommandSource>): Int {
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
    protected open fun run(ctx: CommandContext<ServerCommandSource>): Int {
        throw UnsupportedOperationException("The main code for this command has not been implemented.")
    }
}
