package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.model.NormalsBakedQuad;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

import net.minecraft.client.renderer.block.model.BakedQuad;

@Mixin(VertexConsumer.class)
public interface VertexConsumerMixin {
    @Inject(method = "putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;[FFFFF[IIZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LightTexture;lightCoordsWithEmission(II)I"))
    private void applyBakedNormals(
        PoseStack.Pose pose,
        BakedQuad quad,
        float[] brightnesses,
        float red,
        float green,
        float blue,
        float f,
        int[] is,
        int i,
        boolean bl,
        CallbackInfo ci,
        @Local Vector3f generated,
        @Local ByteBuffer data
    ) {
        if (NormalsBakedQuad.hasNormals(quad)) {
            byte nx = data.get(28);
            byte ny = data.get(29);
            byte nz = data.get(30);
            if (nx != 0 || ny != 0 || nz != 0) {
                generated.set(nx / 127f, ny / 127f, nz / 127f);
                generated.mul(pose.normal());
            }
        }
    }
}