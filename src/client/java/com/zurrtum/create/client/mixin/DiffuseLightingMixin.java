package com.zurrtum.create.client.mixin;

import com.mojang.blaze3d.platform.Lighting;
import com.zurrtum.create.client.flywheel.backend.engine.uniform.LevelUniforms;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Lighting.class)
public class DiffuseLightingMixin {
    @Inject(method = "updateBuffer(Lcom/mojang/blaze3d/platform/Lighting$Entry;Lorg/joml/Vector3f;Lorg/joml/Vector3f;)V", at = @At("TAIL"))
    private void updateBuffer(Lighting.Entry type, Vector3f light0Diffusion, Vector3f light1Diffusion, CallbackInfo ci) {
        LevelUniforms.update(type, light0Diffusion, light1Diffusion);
    }

    @Inject(method = "setupFor(Lcom/mojang/blaze3d/platform/Lighting$Entry;)V", at = @At("TAIL"))
    private void setShaderLights(Lighting.Entry type, CallbackInfo ci) {
        LevelUniforms.set(type);
    }
}
