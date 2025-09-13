package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.content.equipment.armor.CardboardArmorHandlerClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin {
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void tickPost(CallbackInfo ci) {
        CardboardArmorHandlerClient.keepCacheAliveDesignDespiteNotRendering((AbstractClientPlayerEntity) (Object) this);
    }
}
