package com.zurrtum.create.mixin;

import com.zurrtum.create.Create;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftDedicatedServer.class)
public class MinecraftDedicatedServerMixin {
    @Inject(method = "setupServer()Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/MinecraftDedicatedServer;loadWorld()V"))
    private void setupServer(CallbackInfoReturnable<Boolean> cir) {
        Create.SERVER = (MinecraftDedicatedServer) (Object) this;
    }
}
