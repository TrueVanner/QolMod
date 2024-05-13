package net.vannername.qol.mixin;

import com.mojang.authlib.GameProfile;
import eu.pb4.playerdata.api.PlayerDataApi;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.vannername.qol.QoLMod;
import net.vannername.qol.utils.Utils;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerMixin extends LivingEntityMixin {
    private void detect(String message) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        player.sendMessage(Text.literal(message));
        LoggerFactory.getLogger(QoLMod.MOD_ID).info(message);
    }

//    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At("TAIL"))
    public void onItemDrop(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
        detect("Item dropped");
    }

//    @Inject(method = "closeHandledScreen", at = @At("TAIL"))
    public void onHandledScreenClosed(CallbackInfo ci) {
        detect("Closed a handled screen");
    }

//    @Inject(method = "<init>", at = @At(value = "TAIL"))
    public void alterHealth(World world, BlockPos pos, float yaw, GameProfile gameProfile, CallbackInfo ci) {
        ((PlayerEntity) (Object) this).getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).addPersistentModifier(new EntityAttributeModifier("thisnamedosentmatter", -15, EntityAttributeModifier.Operation.ADD_VALUE));
    }
}