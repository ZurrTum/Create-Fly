package com.zurrtum.create.client.mixin;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.zurrtum.create.client.flywheel.backend.gl.GlStateTracker;
import com.zurrtum.create.client.flywheel.backend.gl.buffer.GlBufferType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public class GlStateManagerMixin {
    @Inject(method = "_glBindBuffer(II)V", at = @At("RETURN"))
    private static void flywheel$onBindBuffer(int target, int buffer, CallbackInfo ci) {
        GlStateTracker._setBuffer(GlBufferType.fromTarget(target), buffer);
    }

    @Inject(method = "_glBindVertexArray(I)V", at = @At("RETURN"))
    private static void flywheel$onBindVertexArray(int array, CallbackInfo ci) {
        GlStateTracker._setVertexArray(array);
    }

    @Inject(method = "_glUseProgram(I)V", at = @At("RETURN"))
    private static void flywheel$onUseProgram(int program, CallbackInfo ci) {
        GlStateTracker._setProgram(program);
    }
}
