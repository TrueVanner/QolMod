package me.vannername.qol.mixin;

import me.vannername.qol.main.QoLMod;
import me.vannername.qol.main.config.PlayerConfig;
import me.vannername.qol.main.utils.PlayerUtils;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerMixin extends LivingEntityMixin {
    private void detect(String message) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        player.sendMessage(Text.literal(message), false);
        LoggerFactory.getLogger(QoLMod.MOD_ID).info(message);
    }

    @Override
    public void onPlayerDeath(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity p = (PlayerEntity) (Object) this;

        PlayerConfig data = PlayerUtils.getConfig(p);

        if (data.getSendCoordinatesOfDeath()) {
            p.sendMessage(Text.literal("You died at: ")
                    .append("%1$d %2$d %3$d".formatted((int) p.getX(), (int) p.getY(), (int) p.getZ()))
                    .formatted(Formatting.RED), false
            );
        }
    }

    //    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At("TAIL"))
    public void onItemDrop(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
        detect("Item dropped");
    }

    //    @Inject(method = "closeHandledScreen", at = @At("TAIL"))
    public void onHandledScreenClosed(CallbackInfo ci) {
        detect("Closed a handled screen");
    }


//    @Inject(method = "attack", at = @At(value = "TAIL"), cancellable = true)
//    public void preventFriendlyDamage(Entity target, CallbackInfo ci) {
//    }
}