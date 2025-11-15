package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.BakedItemModelBufferer.ItemMeshEmitterProvider;
import com.zurrtum.create.client.flywheel.lib.model.baked.DualVertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @WrapOperation(method = "getSpecialFoilBuffer(Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/rendertype/RenderType;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;)Lcom/mojang/blaze3d/vertex/VertexConsumer;", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexMultiConsumer;create(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lcom/mojang/blaze3d/vertex/VertexConsumer;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private static VertexConsumer getSpecialItemGlintConsumer(
        VertexConsumer first,
        VertexConsumer second,
        Operation<VertexConsumer> original,
        @Local(argsOnly = true) MultiBufferSource provider
    ) {
        if (provider instanceof ItemMeshEmitterProvider) {
            return new DualVertexConsumer(first, second);
        } else {
            return original.call(first, second);
        }
    }

    @WrapOperation(method = "getFoilBuffer(Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/rendertype/RenderType;ZZ)Lcom/mojang/blaze3d/vertex/VertexConsumer;", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexMultiConsumer;create(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lcom/mojang/blaze3d/vertex/VertexConsumer;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private static VertexConsumer getItemGlintConsumer(
        VertexConsumer first,
        VertexConsumer second,
        Operation<VertexConsumer> original,
        @Local(argsOnly = true) MultiBufferSource provider
    ) {
        if (provider instanceof ItemMeshEmitterProvider) {
            return new DualVertexConsumer(first, second);
        } else {
            return original.call(first, second);
        }
    }
}
