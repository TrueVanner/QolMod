package me.vannername.qol.main.commands

/*
import me.vannername.qol.QoLMod
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager

// TODO: finish this?
class ConfigureProperty {
    init {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                CommandManager.literal("editconfig").executes { ctx ->

//                    val p = ctx.source.playerOrThrow
//                    println(p.hasConfig())
//                    ConfigApi.openScreen("${QoLMod.MOD_ID}.${p.uuid}")
//                    p.getConfig().sendCoordinatesAboveHotbar = !p.getConfig().sendCoordinatesAboveHotbar
////                    println(ConfigApi.deserializeConfig(p.getConfig(), String(Files.readAllBytes(Path.of("config/${QoLMod.MOD_ID}/${p.uuid}"))), mutableListOf()))
                    1
                }
            )
        }

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                ClientCommandManager.literal("editconfig").executes { ctx ->
                    ctx.source.player.networkHandler.sendCommand("/configure ${QoLMod.MOD_ID} ${ctx.source.player.uuid}")
                    1
                }
            )
        }
    }
}
*/
