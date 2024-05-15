package net.vannername.qol.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.BoolArgumentType.getBool
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType.getInteger
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.context.CommandContextBuilder
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandSource
import net.minecraft.command.CommandSource.RelativePosition
import net.minecraft.command.argument.BlockPosArgumentType
import net.minecraft.command.suggestion.SuggestionProviders
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.vannername.qol.utils.PlayerConfig
import net.vannername.qol.utils.PlayerUtils.getConfig
//import net.vannername.qol.utils.ConfigUtils.configurableProps
import net.vannername.qol.utils.Utils
import net.vannername.qol.utils.Utils.toRelativePos
import net.vannername.qol.utils.WorldBlockPos


class Navigate {
    init {
//        val suggestX: SuggestionProvider<ServerCommandSource> = SuggestionProviders.register(Utils.MyIdentifier("suggest_x"),
//            suggestCoord(listOf("~", "0")))
//        val suggestY: SuggestionProvider<ServerCommandSource> = SuggestionProviders.register(Utils.MyIdentifier("suggest_y"),
//            suggestCoord(listOf("~", "0")))
//        val suggestZ: SuggestionProvider<ServerCommandSource> = SuggestionProviders.register(Utils.MyIdentifier("suggest_z"),
//            suggestCoord(listOf("~", "0")))

        val commandNode = CommandManager
            .literal("navigate")
            .build()

        val xNode = CommandManager
            .argument("x", IntegerArgumentType.integer())
            .suggests(BlockPosArgumentType()::listSuggestions)
            .build()

        val yNode = CommandManager
            .argument("y", IntegerArgumentType.integer())
            .suggests(BlockPosArgumentType()::listSuggestions)
            .build()

        val zNode = CommandManager
            .argument("z", IntegerArgumentType.integer())
            .suggests(BlockPosArgumentType()::listSuggestions)
            .executes { ctx ->
                startNavigation(
                    getInteger(ctx, "x"), getInteger(ctx, "y"), getInteger(ctx, "z"), false, ctx
                )
            }
            .build()

        val isDirectNode = CommandManager
            .argument("isDirect", BoolArgumentType.bool())
            .suggests(BoolArgumentType.bool()::listSuggestions)
            .executes { ctx ->
                startNavigation(
                    getInteger(ctx, "x"), getInteger(ctx, "y"),getInteger(ctx, "z"), getBool(ctx, "isDirect"), ctx
                )
            }
            .build()

        val stopNode = CommandManager
            .literal("stop")
            .executes(::stopNavigation)
            .build()


        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.root.addChild(commandNode)
            commandNode.addChild(stopNode)
            commandNode.addChild(xNode)
            xNode.addChild(yNode)
            yNode.addChild(zNode)
            zNode.addChild(isDirectNode)
        }
    }

    @Throws(CommandSyntaxException::class)
    private fun startNavigation(x: Int, y: Int, z: Int, isDirect: Boolean, ctx: CommandContext<ServerCommandSource>): Int {
        val p = ctx.source.playerOrThrow

        p.getConfig().navData = PlayerConfig.PlayerNavigationData(true, WorldBlockPos(x, y, z, p.world.registryKey), isDirect)

        return 1
    }

    @Throws(CommandSyntaxException::class)
    private fun stopNavigation(ctx: CommandContext<ServerCommandSource>): Int {
        val p = ctx.source.playerOrThrow

        p.getConfig().navData.isNavigating = false

        return 1
    }
}