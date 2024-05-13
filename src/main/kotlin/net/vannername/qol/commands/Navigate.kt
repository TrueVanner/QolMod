package net.vannername.qol.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.BoolArgumentType.getBool
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType.getInteger
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import me.x150.renderer.event.RenderEvents
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandSource
import net.minecraft.command.suggestion.SuggestionProviders
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Items
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.SimpleNamedScreenHandlerFactory
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier
//import net.vannername.qol.utils.ConfigUtils.configurableProps
import net.vannername.qol.utils.Utils


class Navigate {
    init {
        val suggestCoord = { values: List<String> ->
                { _: CommandContext<CommandSource>, builder: SuggestionsBuilder? ->
                CommandSource.suggestMatching(
                    values.stream(),
                    builder
                )
            }
        }

        val suggestX: SuggestionProvider<ServerCommandSource> = SuggestionProviders.register(Utils.MyIdentifier("suggest_x"),
            suggestCoord(listOf("~", "0")))
        val suggestY: SuggestionProvider<ServerCommandSource> = SuggestionProviders.register(Utils.MyIdentifier("suggest_y"),
            suggestCoord(listOf("~", "0")))
        val suggestZ: SuggestionProvider<ServerCommandSource> = SuggestionProviders.register(Utils.MyIdentifier("suggest_z"),
            suggestCoord(listOf("~", "0")))

        val commandNode = CommandManager
            .literal("navigate")
            .build()

        val xNode = CommandManager
            .argument("x", IntegerArgumentType.integer())
            .suggests(suggestX)
            .build()

        val yNode = CommandManager
            .argument("y", IntegerArgumentType.integer())
            .suggests(suggestY)
            .build()

        val zNode = CommandManager
            .argument("z", IntegerArgumentType.integer())
            .suggests(suggestZ)
            .executes { ctx ->
                run(
                    getInteger(ctx, "x"), getInteger(ctx, "y"), getInteger(ctx, "z"), false, ctx
                )
            }
            .build()

        val isDirect = CommandManager
            .argument("isDirect", BoolArgumentType.bool())
            .suggests(Utils.boolSuggestionProvider)
            .executes { ctx ->
                run(
                    getInteger(ctx, "x"), getInteger(ctx, "y"),getInteger(ctx, "z"), getBool(ctx, "isDirect"), ctx
                )
            }
            .build()


        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.root.addChild(commandNode)
            commandNode.addChild(xNode)
            xNode.addChild(yNode)
            yNode.addChild(zNode)
            zNode.addChild(isDirect)
        }
    }

    @Throws(CommandSyntaxException::class)
    private fun run(x: Int, y: Int, z: Int, isDirect: Boolean, ctx: CommandContext<ServerCommandSource>): Int {
        val p = ctx.source.playerOrThrow

        return 1
    }
}