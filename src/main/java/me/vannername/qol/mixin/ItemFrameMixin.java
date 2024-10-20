package me.vannername.qol.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrameEntity.class)
public abstract class ItemFrameMixin {

    /**
     * Prevents players from interacting with item frames unless they are sneaking.
     * Allows to change whether the frame is visible or not by right-clicking with an amethyst shard.
     */

    private static long time = System.currentTimeMillis();

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void onInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemFrameEntity itemFrame = (ItemFrameEntity) (Object) this;
        if (!player.isSneaking()) {
            cir.setReturnValue(ActionResult.FAIL);
            if (player.getMainHandStack().isOf(Items.AMETHYST_SHARD)
                    || player.getOffHandStack().isOf(Items.AMETHYST_SHARD)) {
                // to prevent multiple interactions in a short time
                if (System.currentTimeMillis() - time > 100) {
                    itemFrame.setInvisible(!itemFrame.isInvisible());
                    time = System.currentTimeMillis();
                }
            }
        }
    }

    @Inject(method = "dropHeldStack", at = @At("HEAD"), cancellable = true)
    private void onDropHeldStack(Entity entity, boolean alwaysDrop, CallbackInfo ci) {
        if (entity instanceof PlayerEntity player && !player.isSneaking()) {
            ci.cancel();
        }
    }
}