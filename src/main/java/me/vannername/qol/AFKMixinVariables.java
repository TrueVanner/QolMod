package me.vannername.qol;

import me.vannername.qol.utils.PlayerUtils;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

abstract public class AFKMixinVariables {
    private static int nrInputs = -1; // -1 required for some reason
    private static int overlayColor = OverlayColor.BEFORE_CLICK.color;
    private static boolean shouldLeaveAFKOnNextInput = false;

    public static boolean isAFK() throws NullPointerException {
        return GlobalMixinVariables.getPlayerConfig().isAFK();
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
                PlayerUtils.INSTANCE.stopAFK(GlobalMixinVariables.getClientPlayer());
                shouldLeaveAFKOnNextInput = false;
                nrInputs = -1;
            } else {
                shouldLeaveAFKOnNextInput = true;
                GlobalMixinVariables.getClientPlayer().sendMessage(Text.literal("Press any key again to exit AFK mode.").formatted(Formatting.AQUA));
                overlayColor = OverlayColor.AFTER_CLICK.color;

                Runnable resetAFK = () -> {
                    try {
                        if (shouldLeaveAFKOnNextInput) {
                            Thread.sleep(1000 * 5);
                            shouldLeaveAFKOnNextInput = false;
                            GlobalMixinVariables.getClientPlayer().sendMessage(Text.literal("AFK mode not aborted.").formatted(Formatting.RED));
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
}
