package net.vannername.qol.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.vannername.qol.schemes.PlayerData;
import net.vannername.qol.utils.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(ServerPlayerEntity.class)
abstract public class ServerPlayerMixin extends LivingEntityMixin {
    @Override
    public void onPlayerDeath(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity p = (ServerPlayerEntity) (Object) this;
        PlayerData data = Utils.getPlayerData(p);
        if(data.getSendDeathCoords()) {
            p.sendMessage(Text.literal("You died at: ")
                    .append("%1$d %2$d %3$d".formatted((int) p.getX(), (int) p.getY(), (int) p.getZ()))
                    .withColor(Color.RED.getRGB())
            );
        }
    }
}
