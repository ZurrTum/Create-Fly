package com.zurrtum.create.client.mixin;

import com.zurrtum.create.Create;
import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IntegratedServer.class)
public class IntegratedServerMixin {
    @Inject(method = "setupServer()Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/integrated/IntegratedServer;loadWorld()V"))
    private void setupServer(CallbackInfoReturnable<Boolean> cir) {
        Create.SERVER = (IntegratedServer) (Object) this;
    }
}
