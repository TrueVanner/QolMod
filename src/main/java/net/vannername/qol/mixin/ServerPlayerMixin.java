package net.vannername.qol.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerEntity.class)
abstract public class ServerPlayerMixin extends LivingEntityMixin {

}
