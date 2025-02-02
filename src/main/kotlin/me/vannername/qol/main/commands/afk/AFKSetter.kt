package me.vannername.qol.main.commands.afk

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import me.vannername.qol.main.commands.afk.AFKUtils.startAFK
import me.vannername.qol.main.commands.util.ServerCommandHandlerBase
import me.vannername.qol.main.utils.PlayerUtils.getConfig
import me.vannername.qol.main.utils.Utils.sendCommandError
import net.minecraft.command.CommandSource
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object AFKSetter : ServerCommandHandlerBase("afk") {

    override fun init() {
        setDefaultAction(::startAFK)
        super.init()
    }

    private enum class AFKSuggestionProviderKeys : SuggestionProviderKey {
        PLAYERNAMES;

        override fun key(): String = this.name
    }

    private enum class AFKCommandNodeKeys : CommandNodeKey {
//        START,
//        STOP,
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

//        CommandManager
//            .literal("start")
//            .executes(::startAFK)
//            .register(AFKCommandNodeKeys.START)

//        CommandManager
//            .literal("stop")
//            .executes(::stopAFK)
//            .register(AFKCommandNodeKeys.STOP)

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
//            AFKCommandNodeKeys.START,
//            AFKCommandNodeKeys.STOP,
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


    // command input when in AFK state is impossible - this can never be trigerred
//    @Throws(CommandSyntaxException::class)
//    fun stopAFK(ctx: CommandContext<ServerCommandSource>): Int {
//        val p = ctx.source.playerOrThrow
//        if (p.getConfig().isAFK) {
//            p.stopAFK()
//            return 1
//        } else {
//            return ctx.sendCommandError("You are not AFK!")
//        }
//    }

    fun suggestAFK(ctx: CommandContext<ServerCommandSource>, player: PlayerEntity): Int {
        return 1
    }

    override fun defineHelpMessages() {
        addPathDescriptions(
            listOf(ROOT) to "Same as /afk start",
//            listOf(AFKCommandNodeKeys.START) to "Starts AFK mode",
//            listOf(AFKCommandNodeKeys.STOP) to "Leave AFK mode"
        )
    }
}