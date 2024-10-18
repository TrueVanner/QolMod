package me.vannername.qol.commands.util

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import me.vannername.qol.main.commands.util.ServerCommandHandlerBase
import me.vannername.qol.main.utils.Utils.appendCommandSuggestion
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

/**
 * Template for a command handler.
 * NEVER use / before the command!
 */
object GetCoords : ServerCommandHandlerBase("getcoords", listOf("gc")) {

    override fun init() {
        setDefaultAction(::run)
        super.init()
    }

    @Throws(CommandSyntaxException::class)
    override fun run(ctx: CommandContext<ServerCommandSource>): Int {
        val p = ctx.source.playerOrThrow
        p.server.sendMessage(
            Text
                .literal("This is a test")
                .appendCommandSuggestion("/navigate 0 0 0")
        )
        return 1
    }
}