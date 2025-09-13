package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.flywheel.backend.engine.uniform.LevelUniforms;
import net.minecraft.client.render.DiffuseLighting;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DiffuseLighting.class)
public class DiffuseLightingMixin {
    @Inject(method = "updateBuffer(Lnet/minecraft/client/render/DiffuseLighting$Type;Lorg/joml/Vector3f;Lorg/joml/Vector3f;)V", at = @At("TAIL"))
    private void updateBuffer(DiffuseLighting.Type type, Vector3f light0Diffusion, Vector3f light1Diffusion, CallbackInfo ci) {
        LevelUniforms.update(type, light0Diffusion, light1Diffusion);
    }

    @Inject(method = "setShaderLights(Lnet/minecraft/client/render/DiffuseLighting$Type;)V", at = @At("TAIL"))
    private void setShaderLights(DiffuseLighting.Type type, CallbackInfo ci) {
        LevelUniforms.set(type);
    }
}
