package me.vannername.qol.commands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import me.vannername.qol.commands.util.CommandHandlerBase
import me.vannername.qol.utils.PlayerUtils.getConfig
import me.vannername.qol.utils.PlayerUtils.startAFK
import me.vannername.qol.utils.PlayerUtils.stopAFK
import me.vannername.qol.utils.Utils.sendCommandError
import net.fabricmc.api.EnvType
import net.minecraft.command.CommandSource
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object AFKSetter : CommandHandlerBase<ServerCommandSource>("afk", EnvType.SERVER) {

    override fun init() {
        setDefaultAction(::startAFK)
        super.init()
    }

    private enum class AFKSuggestionProviderKeys : SuggestionProviderKey {
        PLAYERNAMES;

        override fun key(): String = this.name
    }

    private enum class AFKCommandNodeKeys : CommandNodeKey {
        START,
        STOP,
        PLAYER_NAME;

        override fun key(): String = this.name
    }

    override fun registerSuggestionProviders() {
        registerSuggestionProvider(AFKSuggestionProviderKeys.PLAYERNAMES)
        { ctx, builder ->
            CommandSource.suggestMatching(
                ctx.source.playerNames.filter { it != ctx.source?.player?.name?.string },
                builder
            )
        }
    }

    override fun registerCommandNodes() {
        super.registerCommandNodes()

        CommandManager
            .literal("start")
            .executes(::startAFK)
            .register(AFKCommandNodeKeys.START)

        CommandManager
            .literal("stop")
            .executes(::stopAFK)
            .register(AFKCommandNodeKeys.STOP)

        CommandManager
            .argument("player", EntityArgumentType.player())
            .suggests { ctx, builder ->
                AFKSuggestionProviderKeys.PLAYERNAMES.getSuggestions(ctx, builder)
            }
            .executes { ctx ->
                suggestAFK(ctx, EntityArgumentType.getPlayer(ctx, "player"))
            }
            .register(AFKCommandNodeKeys.PLAYER_NAME)
    }

    override fun commandStructure() {
        super.commandStructure()

        ROOT.addChildren(
            AFKCommandNodeKeys.START,
            AFKCommandNodeKeys.STOP,
            AFKCommandNodeKeys.PLAYER_NAME
        )
    }

    @Throws(CommandSyntaxException::class)
    fun startAFK(ctx: CommandContext<ServerCommandSource>): Int {
        val p = ctx.source.playerOrThrow

        if (!p.getConfig().isAFK) {
            p.startAFK()
            return 1
        } else {
            ctx.sendCommandError("You are already AFK!")
            return 0
        }
    }

    @Throws(CommandSyntaxException::class)
    fun stopAFK(ctx: CommandContext<ServerCommandSource>): Int {
        val p = ctx.source.playerOrThrow
        if (p.getConfig().isAFK) {
            p.stopAFK()
            return 1
        } else {
            return ctx.sendCommandError("You are not AFK!")
        }
    }

    fun suggestAFK(ctx: CommandContext<ServerCommandSource>, player: PlayerEntity): Int {
        return 1
    }

    override fun defineHelpMessages() {
        addPathDescriptions(
            listOf(ROOT) to "Same as /afk start",
            listOf(AFKCommandNodeKeys.START) to "Starts AFK mode",
            listOf(AFKCommandNodeKeys.STOP) to "Leave AFK mode"
        )
    }
}