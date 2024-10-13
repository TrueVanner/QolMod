package me.vannername.qol.mixin.client.AFKMixins;

import me.vannername.qol.clientutils.AFKMixinVariables;
import me.vannername.qol.clientutils.GlobalMixinVariables;
import me.vannername.qol.mixin.LivingEntityMixin;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class AFKPlayerMixin extends LivingEntityMixin {
    @Inject(method = "tickMovement", at = @At("HEAD"), cancellable = true)
    private void preventMovementWhileAFK(CallbackInfo ci) {
        System.out.print("");
        if (GlobalMixinVariables.playerEnteredServer()) {
            try {
                if (AFKMixinVariables.isAFK()) {
                    ci.cancel();
                }
            } catch (NullPointerException ignored) {
            }
        }
    }
}
