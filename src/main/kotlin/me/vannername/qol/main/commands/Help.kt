package me.vannername.qol.commands.util

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import me.vannername.qol.main.commands.util.ServerCommandHandlerBase
import me.vannername.qol.main.utils.PlayerUtils.sendSimpleMessage
import me.vannername.qol.main.utils.Utils.appendCommandSuggestion
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

/**
 * Template for a command handler.
 * NEVER use / before the command!
 */
object Help : ServerCommandHandlerBase("qolmod") {

    override fun init() {
        setDefaultAction(::run)
        super.init()
    }

    @Throws(CommandSyntaxException::class)
    override fun run(ctx: CommandContext<ServerCommandSource>): Int {
        // just a shortcut for text with basic formatting
        fun commandText(command: String, description: String): MutableText {
            return Text.literal("").appendCommandSuggestion("$command help", command)
                .append(": $description").formatted(Formatting.YELLOW)
        }

        val p = ctx.source.playerOrThrow
        p.sendSimpleMessage(
            "Welcome to Qol Mod help menu! To read more about each command, check out their respective help menus by running /<command> help.",
            Formatting.GREEN
        )
        p.sendSimpleMessage("The following commands are currently supported:", Formatting.AQUA)
        p.sendMessage(commandText("/qolmod", "Opens this menu."))
        p.sendMessage(commandText("/e", "Opens the GUI of your enderchest if you have it in your inventory."))
        p.sendMessage(
            commandText(
                "/serverchest",
                "Opens the GUI of the server chest if you have it in your inventory. Check /svc help for more info."
            )
        )
        p.sendMessage(commandText("/getcoords", "Shares the current coordinates of the player."))
        p.sendMessage(
            commandText(
                "/navigate",
                "Helps with navigating to a specific location. Use /nav help for more info."
            )
        )
        p.sendMessage(
            commandText(
                "/skipnight",
                "(and /skipday): Allows to skip a set amount of nights/days with perfect accuracy (hostile mobs never start to spawn/burn respectively)."
            )
        )
        p.sendMessage(commandText("/home", "Teleports you to your spawn location. Use /home help for more info."))
        p.sendMessage(
            commandText(
                "/afk",
                "Puts you into AFK mode, in which you can't interact with the world but also can't be damaged or moved in any way."
            )
        )
        p.sendSimpleMessage("------------------------------------------")
        p.sendSimpleMessage("Other features provided by the mod:", Formatting.AQUA)
        p.sendSimpleMessage(
            "- To place, remove, or rotate an item in an Item Frame, you now need to sneak. Prevents annoying item frame misclicks.",
            Formatting.YELLOW
        )
        p.sendSimpleMessage(
            "- Placing an Item Frame while holding an Amethyst Shard in your off-hand renders the Item Frame invisible. However, as long as you hold an Amethyst Shard in your off-hand, all invisible item frames around you will glow. Right-clicking an Item Frame with Amethyst shard in the main hand inverts Item Frame visibility.",
            Formatting.YELLOW
        )
        p.sendSimpleMessage(
            "- On death, coordinates of death are sent into your personal chat. This can be configured.",
            Formatting.YELLOW
        )
        p.sendSimpleMessage(
            "- Attacking a friendly entity or another player while sneaking deals no damage.",
            Formatting.YELLOW
        )
        return 1
    }
}