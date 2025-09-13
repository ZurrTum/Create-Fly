package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.AllExtensions;
import com.zurrtum.create.client.flywheel.lib.visualization.VisualizationHelper;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.render.chunk.SectionBuilder;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.BiFunction;

@Mixin(SectionBuilder.class)
public class SectionBuilderMixin {
    @Inject(method = "addBlockEntity", at = @At("HEAD"), cancellable = true)
    private <E extends BlockEntity> void flywheel$tryAddBlockEntity(SectionBuilder.RenderData data, E blockEntity, CallbackInfo ci) {
        if (VisualizationHelper.tryAddBlockEntity(blockEntity)) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "build(Lnet/minecraft/util/math/ChunkSectionPos;Lnet/minecraft/client/render/chunk/ChunkRendererRegion;Lcom/mojang/blaze3d/systems/VertexSorter;Lnet/minecraft/client/render/chunk/BlockBufferAllocatorStorage;)Lnet/minecraft/client/render/chunk/SectionBuilder$RenderData;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/BlockStateModel;addParts(Lnet/minecraft/util/math/random/Random;Ljava/util/List;)V"))
    private void build(
        BlockStateModel model,
        Random random,
        List<BlockModelPart> parts,
        Operation<Void> original,
        @Local(argsOnly = true) ChunkRendererRegion world,
        @Local(ordinal = 2) BlockPos pos,
        @Local BlockState state
    ) {
        if (model instanceof WrapperBlockStateModel wrapper) {
            wrapper.addPartsWithInfo(world, pos, state, random, parts);
        } else {
            original.call(model, random, parts);
        }
    }

    @WrapOperation(method = "build(Lnet/minecraft/util/math/ChunkSectionPos;Lnet/minecraft/client/render/chunk/ChunkRendererRegion;Lcom/mojang/blaze3d/systems/VertexSorter;Lnet/minecraft/client/render/chunk/BlockBufferAllocatorStorage;)Lnet/minecraft/client/render/chunk/SectionBuilder$RenderData;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayers;getBlockLayer(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/render/BlockRenderLayer;"))
    private BlockRenderLayer getLayer(
        BlockState state,
        Operation<BlockRenderLayer> original,
        @Local(argsOnly = true) ChunkRendererRegion world,
        @Local(ordinal = 2) BlockPos pos
    ) {
        BiFunction<BlockRenderView, BlockPos, BlockRenderLayer> customLayer = AllExtensions.LAYER.get(state.getBlock());
        if (customLayer != null) {
            return customLayer.apply(world, pos);
        }
        return original.call(state);
    }
}
