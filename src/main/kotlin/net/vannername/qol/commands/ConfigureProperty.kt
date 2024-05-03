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
        var convertedValue: Any
        try {
            val requiredType = ConfigUtils.ConfigProperties.valueOf(param.uppercase()).type
            if(requiredType !in ConfigUtils.configurableTypes) {
                return commandError(ctx, "This parameter can't be configured, as it doesn't store a primitive value")
            }

            if(onlyProp) {
                try {
                    convertedValue = !toBoolean(getConfig(p, param) as Byte)
                } catch (e: ClassCastException) {
                    return commandError(ctx, "Provide a new value for the property.")
                }
            } else {
                convertedValue = try {
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

                if(convertedValue::class.simpleName != requiredType) {
                    return commandError(ctx, "Config parameter $param only accepts values of type $requiredType as input, you provided ${convertedValue::class.simpleName}")
                }
            }

            var prevValue = getConfig(p, param)

            setConfig(p, param, convertedValue)
            createAndLoadCustomData(p)

            // purely decorative segments
            var prevValueColor = Color.WHITE

            if (prevValue is Byte) {
                prevValue = toBoolean(prevValue)
                prevValueColor = if(prevValue) Color.GREEN else Color.RED
            }

            val convertedValueColor = when(convertedValue) {
                is Boolean -> when(convertedValue) {
                    true -> Color.GREEN
                    else -> Color.RED
                }
                else -> Color.WHITE
            }

            Utils.debug(prevValue, "prev value")
            Utils.debug(convertedValue, "new value")

            p.sendMessage(Text.literal("Property %c1{$param} successfully changed (%c2{$prevValue} -> %c3{$convertedValue})")
                .multiColored(listOf(Color.WHITE, prevValueColor, convertedValueColor), Color.CYAN))
            return 1

        } catch (e: ConfigUtils.ConfigUnknownParamException) {
            return commandError(ctx, "Unknown property.")
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }
    }
}