package net.vannername.qol.mixin;

import me.fzzyhmstrs.fzzy_config.api.ConfigApi;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.vannername.qol.QoLMod;
import net.vannername.qol.schemes.PlayerConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
abstract public class PlayerManagerMixin {
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void onPlayerJoin(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        var uuid = player.getUuid();
        if(!QoLMod.playerConfigs.containsKey(uuid)) {
            QoLMod.playerConfigs.put(uuid, ConfigApi.registerAndLoadConfig(() -> new PlayerConfig(uuid)));
        }
    }
}