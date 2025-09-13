package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.content.contraptions.ContraptionHandlerClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OtherClientPlayerEntity.class)
public class OtherClientPlayerEntityMixin {
    @Inject(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/OtherClientPlayerEntity;updateLimbs(Z)V"))
    private void tick(CallbackInfo ci) {
        ContraptionHandlerClient.preventRemotePlayersWalkingAnimations((OtherClientPlayerEntity) (Object) this);
    }
}
