/*
package me.vannername.qol.mixin;

import me.fzzyhmstrs.fzzy_config.api.ConfigApi;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;
import me.vannername.qol.QoLMod;
import me.vannername.qol.config.PlayerConfig;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
abstract public class PlayerManagerMixin {
    //    @Inject(method = "onPlayerConnect", at = @At("HEAD"))
    private void onPlayerJoin(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        // TODO: check if the config file exists in server files too
        System.out.println("Tried to load player config");
        var uuid = player.getUuid();
        if (!QoLMod.playerConfigs.containsKey(uuid)) {
            System.out.println("Added player config");
            QoLMod.playerConfigs.put(uuid, ConfigApi.registerAndLoadConfig(() -> new PlayerConfig(uuid), RegisterType.BOTH));
        }
    }
}*/
