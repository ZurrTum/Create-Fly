package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.foundation.block.LightControlBlock;
import com.zurrtum.create.foundation.block.SelfEmissiveLightingBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockModelRenderer.BrightnessCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockModelRenderer.class)
public class BlockModelRendererMixin {
    @WrapOperation(method = "render(Lnet/minecraft/world/BlockRenderView;Ljava/util/List;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getLuminance()I"))
    private int getLuminance(
        BlockState state,
        Operation<Integer> original,
        @Local(argsOnly = true) BlockRenderView world,
        @Local(argsOnly = true) BlockPos pos
    ) {
        if (state.getBlock() instanceof LightControlBlock block) {
            return block.getLuminance(world, pos);
        }
        return original.call(state);
    }

    @WrapOperation(method = "renderFlat(Lnet/minecraft/world/BlockRenderView;Ljava/util/List;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/BlockModelRenderer$BrightnessCache;getInt(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/util/math/BlockPos;)I"))
    private int getLuminance(
        BrightnessCache instance,
        BlockState state,
        BlockRenderView world,
        BlockPos lightPos,
        Operation<Integer> original,
        @Local(argsOnly = true) BlockPos pos
    ) {
        if (state.getBlock() instanceof SelfEmissiveLightingBlock) {
            lightPos = pos;
        }
        return original.call(instance, state, world, lightPos);
    }
}
