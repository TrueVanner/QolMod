package me.vannername.qol.mixin.client.AFKMixins;

import me.vannername.qol.clientutils.AFKMixinVariables;
import me.vannername.qol.clientutils.GlobalMixinVariables;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
abstract public class AFKKeyboardMixin {

    @Unique
    KeyBinding[] allowedBinds = new KeyBinding[]{
            MinecraftClient.getInstance().options.playerListKey,
            MinecraftClient.getInstance().options.commandKey,
            MinecraftClient.getInstance().options.chatKey,
            MinecraftClient.getInstance().options.advancementsKey,
            MinecraftClient.getInstance().options.fullscreenKey,
    };

    @Unique
    private static int getKeyCode(KeyBinding keyBinding) {
        return keyBinding.getDefaultKey().getCode();
    }

    @Unique
    private boolean isKeyAllowed(int key) {
        if(key == GLFW.GLFW_KEY_ENTER) {
            return true;
        }
        for(KeyBinding keyBind : allowedBinds) {
            if(getKeyCode(keyBind) == key) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private void updateEnteredEscMenu() {
        // if the player is not on the pause screen when they clicked ESC
        // but entered it right after, they are either entering it
        // from in-game (enteredEscMenu is still false) or from options (enteredEscMenu is true).
        // we only care about the first case.
        if(!(GlobalMixinVariables.currentScreen() instanceof GameMenuScreen)) {
            new Thread(() -> {
                try {
                    Thread.sleep(50);
                    if(GlobalMixinVariables.currentScreen() instanceof GameMenuScreen
                            && !AFKMixinVariables.enteredEscMenu()) {
                        AFKMixinVariables.setEnteredEscMenu(true);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            // if the player is on the pause screen when they clicked ESC
            // but left it right after, they can only be going back into the game.
            new Thread(() -> {
                try {
                    Thread.sleep(50);
                    if (GlobalMixinVariables.currentScreen() == null) {
                        AFKMixinVariables.setEnteredEscMenu(false);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (GlobalMixinVariables.playerEnteredServer()) {
            try {
                if(key == GLFW.GLFW_KEY_ESCAPE) {
                    updateEnteredEscMenu();
                } else {
                    // actions are never prevented if the key pressed is ESC
                    if (AFKMixinVariables.shouldPreventInput() && !isKeyAllowed(key)) {
                        ci.cancel();
                        AFKMixinVariables.incrementNrInputs();
                        AFKMixinVariables.prepareToOrStopAFK();
                    }
                }
            } catch (NullPointerException ignored) {
            }
        }
    }
}
