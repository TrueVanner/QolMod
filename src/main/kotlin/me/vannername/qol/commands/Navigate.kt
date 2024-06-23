package me.vannername.qol.commands

//import net.vannername.qol.utils.ConfigUtils.configurableProps
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.BoolArgumentType.getBool
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import me.vannername.qol.utils.PlayerUtils.getConfig
import me.vannername.qol.utils.PlayerUtils.startNavigation
import me.vannername.qol.utils.PlayerUtils.stopNavigation
import me.vannername.qol.utils.Utils.appendCommandSuggestion
import me.vannername.qol.utils.WorldBlockPos
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.command.argument.BlockPosArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path


class Navigate {

    // TODO: fix compass location
    // TODO: add suggestions for coordinates
    // TODO: tests

    init {
        register()
        detectNavigationEnd()
    }

    /**
     * Only works if the server has the [Coord Finder](https://modrinth.com/mod/coord-finder/version/fabric-1.20.6-1.1.0) mod installed.
     * Transforms the positions specified in the config file of the mod to the list of positions in the current mod's format.
     *
     * @return the list of the positions on the server stored in positions.properties
     */
    private fun decomposeCoordsLocations(): List<WorldBlockPos>? {
        try {
            val locations = Files.readAllLines(Path.of("config/coordfinder/places.properties"))
            return locations.map { line ->
                val coords = line.split("=")[1].split(",")
                WorldBlockPos(coords[0].toInt(), coords[1].toInt(), coords[2].toInt(), coords[3])
            }
        } catch (_: IOException) {
            // if the file doesn't exist
            return null
        }
    }
//        SuggestionProviders.register(Utils.MyIdentifier(""))
//        { _: CommandContext<CommandSource>, builder: SuggestionsBuilder? ->
//            CommandSource.suggestMatching(
//                configurableProps.map { prop -> prop.text },
//                builder
//            )
//        }

    private fun register() {
        val commandNode = CommandManager
            .literal("navigate")
            .build()

        val continueNode = CommandManager
            .literal("continue")
            .executes(::continueNavigation)
            .build()

        val coordsNode = CommandManager
            .argument("coords", BlockPosArgumentType.blockPos())
//            .suggests(BlockPosArgumentType()::listSuggestions)
//            .suggests { ctx, builder ->
//                SuggestionProviders.ASK_SERVER.getSuggestions(ctx, builder)
//            }
            .executes { ctx ->
                startNavigation(
                    BlockPosArgumentType.getBlockPos(ctx, "coords"), false, ctx
                )
            }
            .build()

        val isDirectNode = CommandManager
            .argument("isDirect", BoolArgumentType.bool())
            .suggests(BoolArgumentType.bool()::listSuggestions)
            .executes { ctx ->
                startNavigation(
                    BlockPosArgumentType.getBlockPos(ctx, "coords"), getBool(ctx, "isDirect"), ctx
                )
            }
            .build()

        val stopNode = CommandManager
            .literal("stop")
            .executes(::stopNavigation)
            .build()


        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.root.addChild(commandNode)

            commandNode.addChild(coordsNode)
            commandNode.addChild(stopNode)
            commandNode.addChild(continueNode)

            coordsNode.addChild(isDirectNode)
        }
    }

    /**
     * Starts navigation to the specified coordinates.
     * @see me.vannername.qol.utils.PlayerUtils.startNavigation
     */
    @Throws(CommandSyntaxException::class)
    private fun startNavigation(position: BlockPos, isDirect: Boolean, ctx: CommandContext<ServerCommandSource>): Int {
        val p = ctx.source.playerOrThrow
        p.startNavigation(WorldBlockPos(position, p.world.registryKey), isDirect)
        return 1
    }

    /**
     * Stops navigation.
     * @see me.vannername.qol.utils.PlayerUtils.stopNavigation
     */
    @Throws(CommandSyntaxException::class)
    private fun stopNavigation(ctx: CommandContext<ServerCommandSource>): Int {
        val p = ctx.source.playerOrThrow
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
            p.sendMessage(
                Text.literal("You haven't yet started navigating or have already reached your destination")
                    .formatted(Formatting.RED)
            )
            return 0
        }
        if (!navData.target.get().isInSameWorld(p.world)) {
            p.sendMessage(
                Text.literal("You are not in the same world as your destination.")
                    .formatted(Formatting.RED)
            )
            return 0
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
                        p.sendMessage(Text.literal("Navigation stopped.").formatted(Formatting.AQUA))
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
}