package net.vannername.qol.utils

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import eu.pb4.playerdata.api.PlayerDataApi
import net.minecraft.command.CommandSource
import net.minecraft.command.suggestion.SuggestionProviders
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.vannername.qol.QoLMod.DATA_STORAGE
import net.vannername.qol.schemes.PlayerData
import java.awt.Color
import java.util.regex.Pattern

object Utils {
    @JvmStatic
    fun getIntegerLocation(p: PlayerEntity): List<Int> {
        return listOf(p.x.toInt(), p.y.toInt(), p.z.toInt())
    }

    @JvmStatic
    fun commandError(context: CommandContext<ServerCommandSource>, text: String): Int {
        context.source.sendError(Text.literal(text))
        return 0
    }

    @JvmStatic
    fun ServerPlayerEntity.getPlayerData(): PlayerData {
        return PlayerDataApi.getCustomDataFor(this, DATA_STORAGE)!!
    }

    @JvmStatic
    fun ServerPlayerEntity.setPlayerData(data: PlayerData) {
        PlayerDataApi.setCustomDataFor(this, DATA_STORAGE, data)
    }

    /**
     * Transforms the Text by coloring the specified segments according to the specified colors.
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
        while(matcher.hasMatch()) {

            // if the current index is NOT the start of the next match (a.k.a text that is normal)
            if(currentIndex < matcher.start()) {
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

                if(colorID < 0 || colorID > colors.size) {
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
       println(if(name != null) "$name: $value" else value)
    }

    // MY collection of the most popular colors.
    enum class Colors(val c: Color) {
        WHITE(Color.WHITE),
        RED(Color.RED),
        GREEN(Color.GREEN),
        BLUE(Color.BLUE),
        YELLOW(Color.YELLOW),
        CYAN(Color.CYAN),
    }

    @JvmStatic
    val boolSuggestionProvider: SuggestionProvider<ServerCommandSource> = SuggestionProviders.register(Identifier("bool"))
    { _: CommandContext<CommandSource>, builder: SuggestionsBuilder? ->
        CommandSource.suggestMatching(
            listOf("true", "false").stream(),
            builder
        )
    }
}