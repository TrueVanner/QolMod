package me.vannername.qol.mixin.AFKMixins;

import me.vannername.qol.AFKMixinVariables;
import me.vannername.qol.GlobalMixinVariables;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
abstract public class AFKInGameHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void renderBlackScreen(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (GlobalMixinVariables.playerEnteredServer()) {
            try {
                if (AFKMixinVariables.isAFK()) {
//                    renderVignetteOverlay(context, null);
                    int screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
                    int screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();

                    context.fill(0, 0, screenWidth, screenHeight, AFKMixinVariables.getOverlayColor());
                }
            } catch (NullPointerException ignored) {
            }
        }
    }
}