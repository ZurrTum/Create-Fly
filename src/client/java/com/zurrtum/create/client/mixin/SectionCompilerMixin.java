package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.AllExtensions;
import com.zurrtum.create.client.flywheel.lib.visualization.VisualizationHelper;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.BiFunction;

@Mixin(SectionCompiler.class)
public class SectionCompilerMixin {
    @Inject(method = "handleBlockEntity", at = @At("HEAD"), cancellable = true)
    private <E extends BlockEntity> void flywheel$tryAddBlockEntity(SectionCompiler.Results data, E blockEntity, CallbackInfo ci) {
        if (VisualizationHelper.tryAddBlockEntity(blockEntity)) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderSectionRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/BlockStateModel;collectParts(Lnet/minecraft/util/RandomSource;Ljava/util/List;)V"))
    private void build(
        BlockStateModel model,
        RandomSource random,
        List<BlockModelPart> parts,
        Operation<Void> original,
        @Local(argsOnly = true) RenderSectionRegion world,
        @Local(ordinal = 2) BlockPos pos,
        @Local BlockState state
    ) {
        if (WrapperBlockStateModel.unwrapCompat(model) instanceof WrapperBlockStateModel wrapper) {
            wrapper.addPartsWithInfo(world, pos, state, random, parts);
        } else {
            original.call(model, random, parts);
        }
    }

    @WrapOperation(method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderSectionRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemBlockRenderTypes;getChunkRenderType(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/renderer/chunk/ChunkSectionLayer;"))
    private ChunkSectionLayer getLayer(
        BlockState state,
        Operation<ChunkSectionLayer> original,
        @Local(argsOnly = true) RenderSectionRegion world,
        @Local(ordinal = 2) BlockPos pos
    ) {
        BiFunction<BlockAndTintGetter, BlockPos, ChunkSectionLayer> customLayer = AllExtensions.LAYER.get(state.getBlock());
        if (customLayer != null) {
            return customLayer.apply(world, pos);
        }
        return original.call(state);
    }
}
