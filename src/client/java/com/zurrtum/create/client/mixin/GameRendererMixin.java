package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.catnip.render.EntityBlockLayer;
import com.zurrtum.create.client.catnip.render.EntityBlockLightLayer;
import com.zurrtum.create.client.catnip.render.EntityBlockMultipleLayer;
import com.zurrtum.create.client.flywheel.impl.event.RenderContextHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyArg(method = "renderLevel()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ProjectionMatrixBuffer;getBuffer(Lorg/joml/Matrix4f;)Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;"))
    private Matrix4f updateProjection(
        Matrix4f projection
    ) {
        ((RenderContextHolder) minecraft.levelRenderer).flywheel$updateProjection(projection);
        return projection;
    }

    @Inject(method = "renderLevel()V", at = @At("TAIL"))
    private void recycleAll(CallbackInfo ci) {
        EntityBlockLightLayer.recycleAll();
        EntityBlockLayer.recycleAll();
        EntityBlockMultipleLayer.recycleAll();
    }
}
