package net.vannername.qol.commands

import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandSource
import net.minecraft.command.suggestion.SuggestionProviders
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.vannername.qol.utils.ConfigUtils
import net.vannername.qol.utils.ConfigUtils.createAndLoadCustomData
import net.vannername.qol.utils.ConfigUtils.getConfig
import net.vannername.qol.utils.ConfigUtils.propertyNames
import net.vannername.qol.utils.ConfigUtils.setConfig
import net.vannername.qol.utils.ConfigUtils.toBoolean
import net.vannername.qol.utils.Utils
import net.vannername.qol.utils.Utils.commandError
import net.vannername.qol.utils.Utils.multiColored
import java.awt.Color
import java.lang.Integer.parseInt

class ConfigureProperty {
    init {

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                CommandManager.literal("setproperty")
                    .then(argument("property", string())
                        .suggests(

                            SuggestionProviders.register(Identifier("all_config_properties"))
                            { _: CommandContext<CommandSource>, builder: SuggestionsBuilder? ->
                                CommandSource.suggestMatching(
                                    propertyNames.stream(),
                                    builder
                                )
                            }

                        )
                        .executes { ctx ->
                            run(
                                getString(ctx, "property"), "", true, ctx
                            )
                        }
                        .then(argument("value", string())
                            .executes { ctx ->
                                run(
                                    getString(ctx,"property"), getString(ctx, "value"), false, ctx
                                )
                            }
                        )
                    )
            )
        }
    }

    @Throws(CommandSyntaxException::class)
    private fun run(param: String, value: String, onlyProp: Boolean, ctx: CommandContext<ServerCommandSource>): Int {

        val p = ctx.source.playerOrThrow
        try {
            val requiredType = ConfigUtils.ConfigProperties.valueOf(param.uppercase()).type
            if(requiredType !in ConfigUtils.configurableTypes) {
                return commandError(ctx, "This parameter can't be configured, as it doesn't store a primitive value")
            }

            val newValue = if(onlyProp) {
                try {
                    !toBoolean(getConfig(p, param) as Byte)
                } catch (e: ClassCastException) {
                    throw RuntimeException("Provide a new value for the property.")
                }
            } else {
                convertValue(value)
            }

            if(newValue::class.simpleName != requiredType) {
                return commandError(ctx, "Config parameter $param only accepts values of type $requiredType as input, you provided ${newValue::class.simpleName}")
            }

            var prevValue = getConfig(p, param)
            if (prevValue is Byte) {
                prevValue = toBoolean(prevValue)
            }

            if(prevValue == newValue) {
                return commandError(ctx, "Nothing changed. The value of $param was already $prevValue")
            }

            setConfig(p, param, newValue)
            createAndLoadCustomData(p)

            p.sendMessage(Text.literal("Property %c1{$param} successfully changed (%c2{$prevValue} -> %c3{$newValue})")
                .multiColored(listOf(Color.WHITE, getColor(prevValue), getColor(newValue)), Color.CYAN))
            return 1

        } catch (e: ConfigUtils.ConfigUnknownParamException) {
            return commandError(ctx, "Unknown property.")
        } catch (e: RuntimeException) {
          return commandError(ctx, e.message!!)
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }
    }

    /**
     * Generates text color for the value. White by default, red if the value is false and green if true. Purely decorative
     */
    private fun getColor(value: Any): Color {
        return when(value) {
            is Boolean -> when(value) {
                true -> Color.GREEN
                else -> Color.RED
            }
            else -> Color.WHITE
        }
    }

    private fun convertValue(value: String): Any {
        return try {
            // Int?
            parseInt(value)
        } catch (e: NumberFormatException) {
            try {
                // Boolean?
                when(value) {
                    "true" -> true
                    "false" -> false
                    else -> throw ClassCastException()
                }
            } catch (e: ClassCastException) {
                value
            }
        }
    }
}