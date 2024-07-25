package me.vannername.qol.mixin.AFKMixins;

import me.vannername.qol.AFKMixinVariables;
import me.vannername.qol.GlobalMixinVariables;
import net.minecraft.client.Keyboard;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
abstract public class AFKKeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (GlobalMixinVariables.playerEnteredServer()) {
            try {
                if (AFKMixinVariables.isAFK() && key != GLFW.GLFW_KEY_ESCAPE) {
                    ci.cancel();
                    AFKMixinVariables.incrementNrInputs();
                    AFKMixinVariables.prepareToOrStopAFK();
                }
            } catch (NullPointerException ignored) {
            }
        }
    }
}
