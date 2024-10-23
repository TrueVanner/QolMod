package me.vannername.qol.clientutils;

import me.fzzyhmstrs.fzzy_config.api.ConfigApi;
import me.vannername.qol.main.networking.payloads.AFKPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

abstract public class AFKMixinVariables {
    private static boolean isAFK = false;
    private static int nrInputs = -1; // -1 required for some reason
    private static int overlayColor = OverlayColor.BEFORE_CLICK.color;
    private static boolean shouldLeaveAFKOnNextInput = false;
    private static boolean enteredEscMenu = false;
    private static boolean ignoreInput = false; // determines whether the system should treat
    // the inputs as attempts to exit AFK mode.

    public static boolean isAFK() {
        return isAFK;
    }

    public static void setIsAFK(boolean newState) {
        isAFK = newState;
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

    public static void setIgnoreInput(boolean newState) {
        ignoreInput = newState;
    }

    public static void ignoreInputFor(long delay) {
        ignoreInput = true;
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                ignoreInput = false;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public static void prepareToOrStopAFK() {
        // when player activates AFK using /afk start, the release of
        // the Enter key is also counted, so the counter technically starts
        // at 1. Any further key presses are also counted 2 times
        if (nrInputs % 2 == 1 && !ignoreInput) {
            if (shouldLeaveAFKOnNextInput) {
                // configs are desynced for some reason, but I'm using it here anyway
                // (along with sending the packet

                AFKMixinVariables.setIsAFK(false);
                // inform the server that AFK mode can now be aborted
                ConfigApi.INSTANCE.network().send(new AFKPayload(false), GlobalMixinVariables.getClientPlayer());

                shouldLeaveAFKOnNextInput = false;
                nrInputs = -1;
            } else {
                shouldLeaveAFKOnNextInput = true;
                GlobalMixinVariables.getClientPlayer().sendMessage(Text.literal("Press any key again to exit AFK mode.").formatted(Formatting.AQUA), false);
                overlayColor = OverlayColor.AFTER_CLICK.color;

                Runnable resetAFK = () -> {
                    try {
                        Thread.sleep(5000);
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

    /**
     * Determines whether the mouse and keyboard should prevent any input.
     * Should always be true when the player is AFK, except if they are
     * typing in chat or are on the ESC menu.
     *
     * @return
     */
    public static boolean shouldPreventInput() {
        return isAFK()
                && !(MinecraftClient.getInstance().currentScreen instanceof ChatScreen)
                && !enteredEscMenu;
    }
}
