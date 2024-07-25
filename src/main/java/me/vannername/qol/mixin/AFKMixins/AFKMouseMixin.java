package me.vannername.qol.mixin.AFKMixins;

import me.vannername.qol.AFKMixinVariables;
import me.vannername.qol.GlobalMixinVariables;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.GameMenuScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
abstract public class AFKMouseMixin {
    @Inject(method = "onCursorPos", at = @At("HEAD"), cancellable = true)
    private void onCursorPos(long window, double xpos, double ypos, CallbackInfo ci) {
        if (GlobalMixinVariables.playerEnteredServer()) {
            try {
                if (AFKMixinVariables.isAFK()
                        && !(MinecraftClient.getInstance().currentScreen instanceof GameMenuScreen)) {
                    ci.cancel();
                }
            } catch (NullPointerException ignored) {
            }
        }
    }

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void onClick(long window, int button, int action, int mods, CallbackInfo ci) {
        if (GlobalMixinVariables.playerEnteredServer()) {
            try {
                if (AFKMixinVariables.isAFK()
                        && !(MinecraftClient.getInstance().currentScreen instanceof GameMenuScreen)) {
                    ci.cancel();
                    AFKMixinVariables.incrementNrInputs();
                    AFKMixinVariables.prepareToOrStopAFK();
                }
            } catch (NullPointerException ignored) {
            }
        }
    }
}
