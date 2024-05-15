package net.vannername.qol.commands


import com.mojang.brigadier.arguments.BoolArgumentType
import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager
import net.vannername.qol.QoLMod
import net.vannername.qol.utils.PlayerUtils.getConfig
import net.vannername.qol.utils.PlayerUtils.hasConfig

class ConfigureProperty {
    init {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                CommandManager.literal("editconfig").executes { ctx ->
                    val p = ctx.source.playerOrThrow
                    println(p.hasConfig())
                    ConfigApi.openScreen("${QoLMod.MOD_ID}.${p.uuid}")
                    p.getConfig().sendCoordinatesAboveHotbar = !p.getConfig().sendCoordinatesAboveHotbar
//                    println(ConfigApi.deserializeConfig(p.getConfig(), String(Files.readAllBytes(Path.of("config/${QoLMod.MOD_ID}/${p.uuid}"))), mutableListOf()))
                    1
                }
            )
        }
    }
}
