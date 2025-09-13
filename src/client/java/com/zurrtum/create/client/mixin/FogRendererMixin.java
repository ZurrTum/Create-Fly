package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.flywheel.backend.engine.uniform.FogUniforms;
import net.minecraft.client.render.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

@Mixin(FogRenderer.class)
public class FogRendererMixin {
    @Inject(method = "applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V", at = @At("TAIL"))
    private void applyFog(
        ByteBuffer buffer,
        int bufPos,
        Vector4f fogColor,
        float environmentalStart,
        float environmentalEnd,
        float renderDistanceStart,
        float renderDistanceEnd,
        float skyEnd,
        float cloudEnd,
        CallbackInfo ci
    ) {
        FogUniforms.update(fogColor, environmentalStart, environmentalEnd, renderDistanceStart, renderDistanceEnd);
    }
}
