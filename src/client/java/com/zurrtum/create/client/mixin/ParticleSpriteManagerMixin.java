package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.AllParticleTypes;
import net.minecraft.client.particle.ParticleResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleResources.class)
public class ParticleSpriteManagerMixin {
    @Inject(method = "registerProviders()V", at = @At("RETURN"))
    private void init(CallbackInfo ci) {
        AllParticleTypes.register((ParticleResources) (Object) this);
    }
}
