package me.vannername.qol.mixin;

import me.vannername.qol.QoLMod;
import me.vannername.qol.main.config.PlayerConfig;
import me.vannername.qol.main.utils.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PlayerEntity.class)
public abstract class PlayerMixin extends LivingEntityMixin {
    private void detect(String message) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        player.sendMessage(Text.literal(message), false);
        LoggerFactory.getLogger(QoLMod.MOD_ID).info(message);
    }

    @Override
    public void onPlayerDeath(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
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

    @Unique
    private static List<EntityType> hostiles = List.of(
            EntityType.BLAZE,
            EntityType.ENDERMITE,
            EntityType.ENDERMAN,
            EntityType.SLIME,
            EntityType.ZOMBIE,
            EntityType.PIGLIN_BRUTE,
            EntityType.PIGLIN,
            EntityType.SKELETON,
            EntityType.BAT,
            EntityType.SPIDER,
            EntityType.CAVE_SPIDER,
            EntityType.ZOMBIFIED_PIGLIN,
            EntityType.EVOKER,
            EntityType.VINDICATOR,
            EntityType.PILLAGER,
            EntityType.RAVAGER,
            EntityType.VEX,
            EntityType.GUARDIAN,
            EntityType.ELDER_GUARDIAN,
            EntityType.SHULKER,
            EntityType.HUSK,
            EntityType.STRAY,
            EntityType.PHANTOM,
            EntityType.CREEPER,
            EntityType.GHAST,
            EntityType.MAGMA_CUBE,
            EntityType.SILVERFISH,
            EntityType.ZOMBIE_VILLAGER,
            EntityType.DROWNED,
            EntityType.WITHER_SKELETON,
            EntityType.WITCH,
            EntityType.HOGLIN,
            EntityType.ZOGLIN,
            EntityType.WARDEN,
            EntityType.ENDER_DRAGON,
            EntityType.WITHER,
            EntityType.FIREBALL,
            EntityType.BREEZE,
            EntityType.SHULKER_BULLET
    );

    @Inject(method = "attack", at = @At(value = "TAIL"), cancellable = true)
    public void preventFriendlyDamage(Entity target, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (player.isSneaking() && !hostiles.contains(target.getType())) {
            ci.cancel();
            player.getWorld().addParticle(
                    ParticleTypes.HEART,
                    target.getX() - 0.5 + Math.random(),
                    target.getHeight() - 0.25 + Math.random() * 0.5,
                    target.getZ() - 0.5 + Math.random(),
                    Math.random(), Math.random(), Math.random());
        }
//        .getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).addPersistentModifier(new EntityAttributeModifier("thisnamedosentmatter", -15, EntityAttributeModifier.Operation.ADD_VALUE));
    }
}