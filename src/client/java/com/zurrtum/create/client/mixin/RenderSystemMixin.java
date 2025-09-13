package com.zurrtum.create.client.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zurrtum.create.client.flywheel.backend.engine.uniform.FogUniforms;
import com.zurrtum.create.client.flywheel.backend.gl.GlCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {
    @Inject(method = "initRenderer", at = @At("RETURN"))
    private static void flywheel$onInitRenderer(CallbackInfo ci) {
        GlCompat.init();
    }

    @Inject(method = "setShaderFog(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V", at = @At("RETURN"))
    private static void flywheel$onSetShaderFog(GpuBufferSlice shaderFog, CallbackInfo ci) {
        FogUniforms.update(shaderFog);
    }
}
