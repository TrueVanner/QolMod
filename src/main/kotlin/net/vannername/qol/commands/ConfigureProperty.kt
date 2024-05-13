package net.vannername.qol.commands

import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandSource
import net.minecraft.command.suggestion.SuggestionProviders
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.vannername.qol.utils.ConfigUtils
import net.vannername.qol.utils.ConfigUtils.bool
import net.vannername.qol.utils.ConfigUtils.color
import net.vannername.qol.utils.ConfigUtils.configurableProps
import net.vannername.qol.utils.ConfigUtils.createAndLoadCustomData
import net.vannername.qol.utils.ConfigUtils.getConfig
import net.vannername.qol.utils.ConfigUtils.int
import net.vannername.qol.utils.ConfigUtils.setConfig
import net.vannername.qol.utils.Utils
import net.vannername.qol.utils.Utils.commandError
import net.vannername.qol.utils.Utils.multiColored
import java.lang.Integer.parseInt

class ConfigureProperty {
    init {
        val suggestProperty: SuggestionProvider<ServerCommandSource> = SuggestionProviders.register(Identifier("qol_mod", "all_config_properties"))
            { _: CommandContext<CommandSource>, builder: SuggestionsBuilder? ->
                CommandSource.suggestMatching(
                    configurableProps.map { prop -> prop.text },
                    builder
                )
            }

        val suggestValue: SuggestionProvider<ServerCommandSource> = SuggestionProviders.register(Identifier("qol_mod", "potential_config_values"))
            { ctx: CommandContext<CommandSource>, builder: SuggestionsBuilder? ->
                val possibleValuesList: List<String> = when(ConfigUtils.ConfigProperty.typeOf(getString(ctx, "property"))) {
                    bool -> listOf("true", "false")
                    int -> listOf("0")
                    color -> Utils.Colors.entries.map { entry -> entry.toString() }
                    else -> emptyList()
                }
                CommandSource.suggestMatching(
                    possibleValuesList.stream(),
                    builder
                )
            }

        val commandNode = CommandManager
            .literal("setproperty")
            .build()

        val propertyNode = CommandManager
            .argument("property", string())
            .suggests(suggestProperty)
            .executes { ctx ->
                run(
                    getString(ctx, "property"), "", true, ctx
                )
            }
            .build()

        val valueNode = CommandManager
            .argument("value", string())
            .suggests(suggestValue)
            .executes { ctx ->
                run(
                    getString(ctx,"property"), getString(ctx, "value"), false, ctx
                )
            }
            .build()

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.root.addChild(commandNode)
            commandNode.addChild(propertyNode)
            propertyNode.addChild(valueNode)
        }
    }

    @Throws(CommandSyntaxException::class)
    private fun run(propName: String, value: String, onlyProp: Boolean, ctx: CommandContext<ServerCommandSource>): Int {
        val prop: ConfigUtils.ConfigProperty

        try {
            prop = ConfigUtils.ConfigProperty.valueOf(propName.uppercase())
            if(prop !in configurableProps) {
                return commandError(ctx, "This property cannot be configured via commands.")
            }
        } catch (e: IllegalArgumentException) {
            return commandError(ctx, "This property doesn't exist.")
        }

        val p = ctx.source.playerOrThrow

        try {
            val newValue = if(onlyProp) {
                try {
                    !p.getConfig(prop, Boolean::class)
                } catch (e: ConfigUtils.ConfigBadTypeException) {
                    throw RuntimeException("Provide a new value for the property.")
                }
            } else {
                toTrueType(value)
            }

            if(newValue::class.simpleName != prop.type.simpleName) {
                return commandError(ctx, "Config parameter $propName only accepts values of type ${prop.type.simpleName} as input, you provided ${newValue::class.simpleName}")
            }

            val prevValue = p.getConfig(prop, prop.type)

            if(prevValue == newValue) {
                return commandError(ctx, "Nothing changed. The value of $propName was already $prevValue")
            }

            p.setConfig(prop, newValue)
            createAndLoadCustomData(p)

            p.sendMessage(Text.literal("Property %c1{$propName} successfully changed (%c2{$prevValue} -> %c3{$newValue})")
                .multiColored(listOf(Utils.Colors.WHITE, getColor(prevValue), getColor(newValue)), Utils.Colors.CYAN))
            return 1

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
    private fun getColor(value: Any): Utils.Colors {
        return when(value) {
            is Boolean -> when(value) {
                true -> Utils.Colors.GREEN
                else -> Utils.Colors.RED
            }
            is Utils.Colors -> value
            else -> Utils.Colors.WHITE
        }
    }
//
//    private fun <T> convertTo(toConvert: String, type: T): T {
//        return toConvert as T
//    }

    private fun toTrueType(value: String): Any {
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
                try {
                    // Color?
                    Utils.Colors.valueOf(value)
                } catch(e: IllegalArgumentException) {
                    value
                }
            }
        }
    }
}