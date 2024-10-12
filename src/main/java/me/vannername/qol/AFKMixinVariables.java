package me.vannername.qol;

import me.fzzyhmstrs.fzzy_config.api.ConfigApi;
import me.vannername.qol.utils.AFKPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

abstract public class AFKMixinVariables {
    private static int nrInputs = -1; // -1 required for some reason
    private static int overlayColor = OverlayColor.BEFORE_CLICK.color;
    private static boolean shouldLeaveAFKOnNextInput = false;
    private static boolean enteredEscMenu = false;

    public static boolean isAFK() {
        return GlobalMixinVariables.getPlayerConfig().isAFK();
    }

    public static void setIsAFK(boolean newState) {
        GlobalMixinVariables.getPlayerConfig().setAFK(newState);
    }

    public static boolean enteredEscMenu() {
        return enteredEscMenu;
    }

    public static void setEnteredEscMenu(boolean newState) {
        enteredEscMenu = newState;
    }

    public static void incrementNrInputs() {
        AFKMixinVariables.nrInputs++;
    }

    public static int getOverlayColor() {
        return overlayColor;
    }

    public static void prepareToOrStopAFK() {
        // when player activates AFK using /afk start, the release of
        // the Enter key is also counted, so the counter technically starts
        // at 1. Any further key presses are also counted 2 times
        if (nrInputs % 2 == 1) {
            if (shouldLeaveAFKOnNextInput) {
                // configs are desynced for some reason, but I'm using it here anyway
                // (along with sending the packet)

                ConfigApi.INSTANCE.network().send(new AFKPayload(false), GlobalMixinVariables.getClientPlayer());
                shouldLeaveAFKOnNextInput = false;
                nrInputs = -1;
            } else {
                shouldLeaveAFKOnNextInput = true;
                GlobalMixinVariables.getClientPlayer().sendMessage(Text.literal("Press any key again to exit AFK mode.").formatted(Formatting.AQUA), false);
                overlayColor = OverlayColor.AFTER_CLICK.color;

                Runnable resetAFK = () -> {
                    try {
                        Thread.sleep(1000 * 5);
                        if (shouldLeaveAFKOnNextInput) {
                            shouldLeaveAFKOnNextInput = false;
                            GlobalMixinVariables.getClientPlayer().sendMessage(Text.literal("AFK mode not aborted.").formatted(Formatting.RED), false);
                            nrInputs = 0;
                            overlayColor = OverlayColor.BEFORE_CLICK.color;
                        }
                    } catch (InterruptedException ignored) {
                    }
                };
                new Thread(resetAFK).start();
            }
        }
    }

    public enum OverlayColor {
        BEFORE_CLICK(0x80000000),
        AFTER_CLICK(0x70000000);

        final int color;

        OverlayColor(int color) {
            this.color = color;
        }
    }

    public static boolean shouldPreventInput() {
        return isAFK()
                && !(MinecraftClient.getInstance().currentScreen instanceof ChatScreen)
                && !enteredEscMenu;
    }
}
