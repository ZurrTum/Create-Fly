package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.AllParticleTypes;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {
    @Inject(method = "registerDefaultFactories()V", at = @At("RETURN"))
    private void init(CallbackInfo ci) {
        AllParticleTypes.register((ParticleManager) (Object) this);
    }
}
