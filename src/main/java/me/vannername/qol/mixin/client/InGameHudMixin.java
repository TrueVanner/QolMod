package me.vannername.qol.mixin.client;

import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(InGameHud.class)
abstract public class InGameHudMixin {
    /* @Inject(method = "render", at = @At("TAIL"))
    private void renderOverlayWhenInvincible(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (GlobalMixinVariables.playerIsInvulnerable() && !AFKMixinVariables.isAFK()) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);

            context.setShaderColor(1.0f, 0.7f, 1.0f, 0.1f);
            context.drawTexture(Identifier.of("textures/misc/vignette.png"), 0, 0, -90, 0.0f, 0.0f, context.getScaledWindowWidth(), context.getScaledWindowHeight(), context.getScaledWindowWidth(), context.getScaledWindowHeight());

            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }

//        if (GlobalMixinVariables.getClientPlayer().isInvulnerable()) {
//            }
    } */
}