package com.zurrtum.create.mixin;

import com.zurrtum.create.Create;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public class DedicatedServerMixin {
    @Inject(method = "initServer()Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/DedicatedServer;loadLevel()V"))
    private void setupServer(CallbackInfoReturnable<Boolean> cir) {
        Create.SERVER = (DedicatedServer) (Object) this;
    }
}
