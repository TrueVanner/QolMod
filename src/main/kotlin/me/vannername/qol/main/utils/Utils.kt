package me.vannername.qol.main.utils

import com.mojang.brigadier.context.CommandContext
import me.vannername.qol.QoLMod
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.*
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import java.awt.Color
import java.util.regex.Pattern

object Utils {
    // shortcuts.

    fun CommandContext<ServerCommandSource>.sendMessage(text: Text) {
        source.sendMessage(text)
    }

    fun CommandContext<ServerCommandSource>.sendSimpleMessage(text: String, formatting: Formatting? = null) {
        val toSend = Text.literal(text)
        if (formatting != null) toSend.formatted(formatting)
        this.sendMessage(toSend)
    }

    fun CommandContext<ServerCommandSource>.sendCommandSuccess(text: String): Int {
        val toSend = Text.literal(text).formatted(Formatting.GREEN)
        this.sendMessage(toSend)
        return 1
    }

    fun CommandContext<ServerCommandSource>.sendCommandError(text: String): Int {
        val toSend = Text.literal(text).formatted(Formatting.RED)
        source.sendError(toSend)
        return 0
    }

    /**
     * Transforms the Text by coloring the specified segments according to the specified colors.
     * If specified, the whole Text is colored with globalColor.
     * Usage:
     * Text.literal("%c1{This} is a %c2{test} message").multiColored(listOf(Color.RED, Color.GREEN))
     * Text.literal("This uses %c{single} color %c{twice}").multiColored(listOf(Color.RED))
     * @return self, multicolored according to the specification
     */
    @JvmStatic
    fun MutableText.multiColored(colors: List<Int>, globalColor: Int = Color.WHITE.rgb): MutableText {
        val pt = Pattern.compile("%c(\\d?)\\{([^}]*)}")
        val matcher = pt.matcher(this.string)

        // initialize output
        val text = Text.literal("").withColor(globalColor)

        var currentIndex = 0 // will be used to properly separate text
        matcher.find() // run the first detection and find the first match

        // continue while matches still exist
        while (matcher.hasMatch()) {

            // if the current index is NOT the start of the next match (a.k.a text that is normal)
            if (currentIndex < matcher.start()) {
                // append the text from current index till the next match (a.k.a normal text) and color it global color
                text.append(Text.literal(this.string.substring(currentIndex, matcher.start())).withColor(globalColor))
                // set current index to the start of the match
                currentIndex = matcher.start()
            } else {
                val colorID: Int = try {
                    matcher.group(1).toInt() - 1
                } catch (e: NumberFormatException) {
                    // use first color by default
                    0
                }

                if (colorID < 0 || colorID > colors.size) {
                    throw RuntimeException("Illegal color index or insufficient number of colors to color the text")
                }

                // append the matched text (a.k.a segment that needs to be colored) with the specified color
                text.append(Text.literal(matcher.group(2)).withColor(colors[colorID]))


                // set current index to the end of the match
                currentIndex = matcher.end()
                // shrink matcher region to ignore previous matches
                matcher.region(matcher.end(), matcher.regionEnd())
                // find next match
                matcher.find()
            }
        }

        // because the loop has to stop as soon as there aren't any matches, all text after the last match
        // is added manually
        return text.append(Text.literal(this.string.substring(currentIndex, this.string.length)))
    }

    @JvmStatic
    fun MutableText.multiColored(colors: List<Colors>, globalColor: Colors = Colors.WHITE): MutableText {
        return this.multiColored(colors.map { color -> color.c.rgb }, globalColor.c.rgb)
    }

    @JvmStatic
    fun debug(value: Any, name: String? = null) {
        println(if (name != null) "$name: $value" else value)
    }

    // MY collection of the most popular colors.
    enum class Colors(val c: Color) {
        WHITE(Color.WHITE),
        RED(Color.RED),
        GREEN(Color.GREEN),
        BLUE(Color.BLUE),
        GOLD(Color(255, 215, 0)),
        YELLOW(Color.YELLOW),
        CYAN(Color.CYAN),
        GRAY(Color.GRAY)
    }

    fun MyIdentifier(id: String): Identifier = Identifier.of(QoLMod.MOD_ID, id)

    fun String.sentenceCase(): String {
        return this[0].uppercase() + this.substring(1).lowercase()
    }

    /**
     * Appends a line of Text that suggests a command upon being clicked.
     * @param command the command that will be suggested
     * @param text the text that will be displayed. The command itself will be displayed
     *  if null.
     */
    fun MutableText.appendCommandSuggestion(command: String, text: String? = null): MutableText {
        val link = Text.literal(text ?: command)
        link.setStyle(
            Style.EMPTY.withClickEvent(
                ClickEvent(
                    ClickEvent.Action.SUGGEST_COMMAND,
                    command
                )
            ).withHoverEvent(
                HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(command))
            )
        )
        link.formatted(Formatting.UNDERLINE)
        return append(link)
    }

    fun broadcast(message: Text) {
        QoLMod.getServer().playerManager.broadcast(message, false)
    }

    fun broadcast(message: String) {
        broadcast(Text.literal(message))
    }
}