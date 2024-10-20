package me.vannername.qol.mixin.client;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerMixin {
//    @Inject(method = "tick", at = @At("TAIL"))
//    private void onTick(CallbackInfo ci) {
//        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
//        if(player.getOffHandStack().isOf(Items.AMETHYST_SHARD)) {
//            List<ItemFrameEntity> itemFrames =
//            player.clientWorld.getEntitiesByClass(
//                    ItemFrameEntity.class,
//                    player.getBoundingBox().expand(5),
//                    Entity::isInvisible
//            );
//            System.out.println(itemFrames.size());
//            for(ItemFrameEntity itemFrame : itemFrames) {
//                itemFrame.setGlowing(true);
//                System.out.println(itemFrame.isGlowing());
//            }
//        }
//    }
}
