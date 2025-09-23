package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(BlockRenderManager.class)
public class BlockRenderManagerMixin {
    @WrapOperation(method = "renderDamage(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/BlockStateModel;addParts(Lnet/minecraft/util/math/random/Random;Ljava/util/List;)V"))
    private void renderDamage(
        BlockStateModel model,
        Random random,
        List<BlockModelPart> parts,
        Operation<Void> original,
        @Local(argsOnly = true) BlockState state,
        @Local(argsOnly = true) BlockPos pos,
        @Local(argsOnly = true) BlockRenderView world
    ) {
        if (WrapperBlockStateModel.unwrapCompat(model) instanceof WrapperBlockStateModel wrapper) {
            wrapper.addPartsWithInfo(world, pos, state, random, parts);
        } else {
            original.call(model, random, parts);
        }
    }
}
