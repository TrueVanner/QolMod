package me.vannername.qol.commands.util

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import me.vannername.qol.main.commands.util.ServerCommandHandlerBase
import me.vannername.qol.main.utils.PlayerUtils.getCoordsString
import me.vannername.qol.main.utils.Utils
import me.vannername.qol.main.utils.Utils.Colors
import me.vannername.qol.main.utils.Utils.appendCommandSuggestion
import me.vannername.qol.main.utils.Utils.multiColored
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting

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
        Utils.broadcast(
            Text
                .literal("Player %c{${p.displayName?.string}} is currently at: ")
                .multiColored(listOf(Colors.WHITE), Colors.GREEN)
                .append(
                    Text.literal("")
                        .appendCommandSuggestion("/navigate ${p.getCoordsString()}", p.getCoordsString())
                        .formatted(Formatting.WHITE)
                )
                .append(Text.literal(" (${p.world.registryKey.value.path})").formatted(Formatting.WHITE))
        )
        return 1
    }
}