package me.vannername.qol.main.commands.navigate

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.BoolArgumentType.getBool
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import me.vannername.qol.QoLMod
import me.vannername.qol.main.commands.navigate.NavigateUtils.startNavigation
import me.vannername.qol.main.commands.navigate.NavigateUtils.stopNavigation
import me.vannername.qol.main.commands.util.ServerCommandHandlerBase
import me.vannername.qol.main.utils.PlayerUtils.getConfig
import me.vannername.qol.main.utils.PlayerUtils.sendSimpleMessage
import me.vannername.qol.main.utils.Utils.appendCommandSuggestion
import me.vannername.qol.main.utils.Utils.sendCommandError
import me.vannername.qol.main.utils.WorldBlockPos
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.command.CommandSource
import net.minecraft.command.argument.BlockPosArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

object Navigate : ServerCommandHandlerBase("navigate", listOf("nav")) {

    // TODO: fix compass location
    // TODO: tests

    override fun init() {
        super.init()
        detectNavigationEnd()
    }

    private val globalCoordsSuggestionProvider = object : SuggestionProviderKey {
        override fun key(): String = "coords-suggestions-global"
    }

    private var suggestionProviderKeysPerWorld:
            Map<Identifier, SuggestionProviderKeyForWorld> = mutableMapOf()

    private class SuggestionProviderKeyForWorld(val worldID: Identifier) : SuggestionProviderKey {
        override fun key(): String = this.worldID.path
    }

    private enum class NavigateCommandNodeKeys : CommandNodeKey {
        CONTINUE,
        COORDS,
        TO_SAVED,
        LOCATION_NAME,
        IS_DIRECT,
        STOP;

        override fun key(): String = this.name
    }

    /**
     * My initial idea was to create a way to send a suggestion
     * that is different from the text that will be placed when the
     * suggestion is selected. This has failed, and now the only purpose
     * of this function is to simplify suggestion code.
     */
//    fun coordSuggestions(worldID: Identifier?, builder: SuggestionsBuilder):
//            CompletableFuture<Suggestions> {
//        for (location in decomposeCoordsLocations(worldID)) {
//            builder.suggest(location.key)
//        }
//        return builder.buildFuture()
//    }

    /**
     * Stores the coordinates saved on the server.
     */
    object SavedLocations {
        private var locations: Map<String, WorldBlockPos> = NavigateUtils.decomposeCoordFinderLocations()

        /**
         * Gets the list of all registered coordinates.
         * Filters by world if worldID is specified.
         */
        fun get(worldID: Identifier? = null): Map<String, WorldBlockPos> {
            val toReturn = locations
            if (worldID == null) return toReturn
            return toReturn.filter { it.value.worldID == worldID.toString() }
        }

        /**
         * Updates the list of all registered coordinates.
         */
        fun update() {
            locations = NavigateUtils.decomposeCoordFinderLocations()
        }

        fun getNames(worldID: Identifier? = null): List<String> {
//        return origin.values.map { it.getString(false) }
            return get(worldID).keys.toList()
        }

        /**
         * Gets a coordinate by name in a "value" format.
         */
        fun getByName(name: String): WorldBlockPos {
            return locations[name]
                ?: throw IllegalArgumentException("No such location found")
        }
    }

    private var suggestionProvidersPerWorldLoaded = false

    private fun registerSuggestionProvidersPerWorld() {
        if (!suggestionProvidersPerWorldLoaded) {
            for (worldID in QoLMod.serverWorldIDs) {
                suggestionProviderKeysPerWorld += worldID to SuggestionProviderKeyForWorld(worldID)
                registerSuggestionProvider(suggestionProviderKeysPerWorld[worldID]!!)
                { _: CommandContext<ServerCommandSource>, builder: SuggestionsBuilder ->
                    CommandSource.suggestMatching(
                        SavedLocations.getNames(worldID), builder
                    )
                }
            }
            suggestionProvidersPerWorldLoaded = true
        }
    }

    override fun registerSuggestionProviders() {
        registerSuggestionProvider(globalCoordsSuggestionProvider)
        { ctx: CommandContext<ServerCommandSource>, builder: SuggestionsBuilder ->
            CommandSource.suggestMatching(
                SavedLocations.getNames(), builder
            )
        }
    }

    override fun registerCommandNodes() {
        super.registerCommandNodes()

        CommandManager
            .literal("continue")
            .executes(::continueNavigation)
            .register(NavigateCommandNodeKeys.CONTINUE)

        CommandManager
            .argument("coords", BlockPosArgumentType.blockPos())
            // if sent by the player, only coordinates in the local world
            // are autofilled. Otherwise, all coordinates.
            .suggests(BlockPosArgumentType.blockPos()::listSuggestions)
            .executes { ctx ->
                val coords = SavedLocations.getByName(StringArgumentType.getString(ctx, "coords"))
                startNavigation(coords, false, ctx)
            }
            .register(NavigateCommandNodeKeys.COORDS)

        CommandManager
            .literal("to_saved")
            .register(NavigateCommandNodeKeys.TO_SAVED)

        CommandManager
            .argument("location_name", StringArgumentType.string())
            .suggests { ctx, builder ->
                registerSuggestionProvidersPerWorld()
                SavedLocations.update()
                val player = ctx.source.player
                if (player != null) {
                    suggestionProviderKeysPerWorld[player.world.registryKey.value]!!
                        .getSuggestions(ctx, builder)
                } else {
                    globalCoordsSuggestionProvider.getSuggestions(ctx, builder)
                }
            }
            .executes { ctx ->
                val coords = SavedLocations.getByName(StringArgumentType.getString(ctx, "location_name"))
                startNavigation(coords, false, ctx)
            }
            .register(NavigateCommandNodeKeys.LOCATION_NAME)

        CommandManager
            .argument("isDirect", BoolArgumentType.bool())
            .suggests(BoolArgumentType.bool()::listSuggestions)
            .executes { ctx ->
                val coords = try {
                    BlockPosArgumentType.getBlockPos(ctx, "coords")
                } catch (e: Exception) {
                    e.printStackTrace()
                    SavedLocations.getByName(StringArgumentType.getString(ctx, "location_name"))
                }
                startNavigation(coords, getBool(ctx, "isDirect"), ctx)
            }
            .register(NavigateCommandNodeKeys.IS_DIRECT, required = false)

        CommandManager
            .literal("stop")
            .executes(::stopNavigation)
            .register(NavigateCommandNodeKeys.STOP)
    }

    override fun commandStructure() {
        super.commandStructure()

        ROOT.addChildren(
            NavigateCommandNodeKeys.COORDS,
            NavigateCommandNodeKeys.STOP,
            NavigateCommandNodeKeys.CONTINUE,
            NavigateCommandNodeKeys.TO_SAVED
        )

        NavigateCommandNodeKeys.TO_SAVED.addChild(
            NavigateCommandNodeKeys.LOCATION_NAME
        )

        NavigateCommandNodeKeys.LOCATION_NAME.addChild(
            NavigateCommandNodeKeys.IS_DIRECT
        )

        NavigateCommandNodeKeys.COORDS.addChild(
            NavigateCommandNodeKeys.IS_DIRECT
        )
    }

    /**
     * Starts navigation to the specified coordinates.
     * @see NavigateUtils.startNavigation
     */
    @Throws(CommandSyntaxException::class)
    private fun startNavigation(position: BlockPos, isDirect: Boolean, ctx: CommandContext<ServerCommandSource>): Int {
        val p = ctx.source.playerOrThrow
        if (p.getConfig().navData.isNavigating) {
            return ctx.sendCommandError("You are already navigating. Use /navigate stop to stop.")
        }
        val wPos = WorldBlockPos(position, p.world.registryKey)
        p.sendSimpleMessage(
            "Distance to destination: ${wPos.distanceTo(WorldBlockPos.ofPlayer(p)).toInt()}",
            Formatting.AQUA
        )
        p.startNavigation(wPos, isDirect)
        return 1
    }

    /**
     * Stops navigation.
     * @see me.vannername.qol.main.utils.PlayerUtils.stopNavigation
     */
    @Throws(CommandSyntaxException::class)
    private fun stopNavigation(ctx: CommandContext<ServerCommandSource>): Int {
        val p = ctx.source.playerOrThrow
        if (!p.getConfig().navData.isNavigating) {
            return ctx.sendCommandError("You are not currently navigating.")
        }
        p.sendSimpleMessage("Navigation stopped.", Formatting.AQUA)
        p.stopNavigation()
        return 1
    }

    /**
     * Continues navigation to the same coordinates if it hasn't been reached.
     */
    @Throws(CommandSyntaxException::class)
    private fun continueNavigation(ctx: CommandContext<ServerCommandSource>): Int {
        val p = ctx.source.playerOrThrow
        val navData = p.getConfig().navData
        if (navData.reached) {
            return ctx.sendCommandError("You haven't yet started navigating or have already reached your destination")
        }
        if (!navData.target.get().isInSameWorld(p.world)) {
            return ctx.sendCommandError("You are not in the same world as your destination.")
        }
        p.startNavigation(navData.target.get(), navData.isDirect)
        return 1
    }

    /**
     * Checks if the player has reached their navigation target.
     * Stops navigation if
     * - the player is right at the target location.
     * - the player is within 5 blocks from the target location (if isDirect is false).
     * - the player changes worlds.
     * Runs every tick.
     */
    private fun detectNavigationEnd() {
        ServerTickEvents.END_SERVER_TICK.register { server ->
            for (p in server.playerManager.playerList) {
                val navData = p.getConfig().navData
                val target = navData.target.get()

                if (navData.isNavigating) {

                    if (!target.isInSameWorld(p.world)) {
                        handleWorldChange(p)
                    }

                    val currentPos = p.blockPos

                    if (currentPos.equals(target) || (!navData.isDirect && target.isWithinDistance(currentPos, 5.0))) {
                        p.stopNavigation()
                        p.sendSimpleMessage("Navigation stopped.", Formatting.AQUA)
                    }
                }
            }
        }
    }

    /**
     * Utility method to stop navigation and inform the player
     * if the player changes worlds.
     */
    private fun handleWorldChange(p: ServerPlayerEntity) {
        p.sendMessage(
            Text.literal("Navigation paused due to changing dimensions. Use ")
                .appendCommandSuggestion("/navigate continue")
                .append(" to continue when you get back.").formatted(Formatting.YELLOW)
        )
    }

    override fun defineHelpMessages() {
        super.defineHelpMessages()

        addPathDescriptions(
            listOf(NavigateCommandNodeKeys.STOP) to "Stops navigation.",
            listOf(
                NavigateCommandNodeKeys.COORDS,
                NavigateCommandNodeKeys.IS_DIRECT
            ) to "Starts navigation to the specified location. If direct, navigation will only be aborted when the player gets to the exact specified block; otherwise, it's aborted when the player gets within a 3x3 area around the specified location.",
        )
    }
}