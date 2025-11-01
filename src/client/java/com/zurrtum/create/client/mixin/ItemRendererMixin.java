package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.flywheel.lib.model.baked.DualVertexConsumer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static com.zurrtum.create.client.flywheel.lib.model.baked.BakedItemModelBufferer.ItemMeshEmitterProvider;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @WrapOperation(method = "getSpecialItemGlintConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack$Entry;)Lnet/minecraft/client/render/VertexConsumer;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumers;union(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/VertexConsumer;)Lnet/minecraft/client/render/VertexConsumer;"))
    private static VertexConsumer getSpecialItemGlintConsumer(
        VertexConsumer first,
        VertexConsumer second,
        Operation<VertexConsumer> original,
        @Local(argsOnly = true) VertexConsumerProvider provider
    ) {
        if (provider instanceof ItemMeshEmitterProvider) {
            return new DualVertexConsumer(first, second);
        } else {
            return original.call(first, second);
        }
    }

    @WrapOperation(method = "getItemGlintConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/RenderLayer;ZZ)Lnet/minecraft/client/render/VertexConsumer;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumers;union(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/VertexConsumer;)Lnet/minecraft/client/render/VertexConsumer;"))
    private static VertexConsumer getItemGlintConsumer(
        VertexConsumer first,
        VertexConsumer second,
        Operation<VertexConsumer> original,
        @Local(argsOnly = true) VertexConsumerProvider provider
    ) {
        if (provider instanceof ItemMeshEmitterProvider) {
            return new DualVertexConsumer(first, second);
        } else {
            return original.call(first, second);
        }
    }
}
