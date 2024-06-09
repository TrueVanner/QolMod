package me.vannername.qol.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
abstract public class ExampleMixin {
    //    @Inject(method = "init()V", at = @At("HEAD"))
    private void init(CallbackInfo info) {
        System.out.println("new line");
    }
}