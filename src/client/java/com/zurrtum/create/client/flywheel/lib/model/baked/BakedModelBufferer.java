package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.flywheel.lib.model.SimpleModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;

final class BakedModelBufferer {
    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(
        ThreadLocalObjects::new);

    private BakedModelBufferer() {
    }

    public static SimpleModel bufferModel(
        BlockStateModel model,
        BlockPos pos,
        BlockAndTintGetter level,
        BlockState state,
        @Nullable PoseStack poseStack,
        BlockMaterialFunction blockMaterialFunction
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        VanillinMeshEmitterManager emitters;
        if (poseStack == null) {
            emitters = objects.emitters;
            emitters.prepare(blockMaterialFunction);
        } else {
            emitters = objects.transformEmitters.prepare(blockMaterialFunction, poseStack);
        }
        ModelRenderHelper.getCurrentThreadAoHelper(emitters)
            .tesselateBlock(0, 0, 0, level, pos, state, model, state.getSeed(pos));
        return emitters.end();
    }

    public static SimpleModel bufferBlock(
        BlockAndTintGetter level,
        @Nullable PoseStack poseStack,
        boolean renderFluids,
        BlockMaterialFunction blockMaterialFunction
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        VanillinMeshEmitterManager emitters;
        if (poseStack == null) {
            emitters = objects.emitters;
            emitters.prepare(blockMaterialFunction);
        } else {
            emitters = objects.transformEmitters.prepare(blockMaterialFunction, poseStack);
        }
        ModelManager modelManager = Minecraft.getInstance().getModelManager();
        BlockState state = level.getBlockState(BlockPos.ZERO);
        if (renderFluids) {
            FluidState fluidState = state.getFluidState();
            if (!fluidState.isEmpty()) {
                emitters.prepareForFluid(BlockPos.ZERO);
                new FluidRenderer(modelManager.getFluidStateModelSet()).tesselate(
                    level,
                    BlockPos.ZERO,
                    emitters,
                    state,
                    fluidState
                );
            }
        }
        if (state.getRenderShape() == RenderShape.MODEL) {
            ModelRenderHelper.getCurrentThreadAoHelper(emitters).tesselateBlock(
                0,
                0,
                0,
                level,
                BlockPos.ZERO,
                state,
                modelManager.getBlockStateModelSet().get(state),
                state.getSeed(BlockPos.ZERO)
            );
        }
        return emitters.end();
    }

    public static SimpleModel bufferBlocks(
        Iterator<BlockPos> posIterator,
        BlockAndTintGetter level,
        @Nullable PoseStack poseStack,
        boolean renderFluids,
        BlockMaterialFunction blockMaterialFunction
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        VanillinMeshEmitterManager emitters;
        if (poseStack == null) {
            emitters = objects.emitters;
            emitters.prepare(blockMaterialFunction);
        } else {
            emitters = objects.transformEmitters.prepare(blockMaterialFunction, poseStack);
        }

        ModelManager modelManager = Minecraft.getInstance().getModelManager();
        BlockStateModelSet blockStateModelSet = modelManager.getBlockStateModelSet();
        FluidRenderer fluidRenderer = new FluidRenderer(modelManager.getFluidStateModelSet());

        ModelConsumer blockRenderer = ModelRenderHelper.getCurrentThreadAoCullHelper(emitters);
        BlockModelLighter.enableCaching();

        while (posIterator.hasNext()) {
            BlockPos pos = posIterator.next();
            BlockState state = level.getBlockState(pos);

            emitters.prepareForBlock();

            if (renderFluids) {
                FluidState fluidState = state.getFluidState();
                if (!fluidState.isEmpty()) {
                    emitters.prepareForFluid(pos);
                    fluidRenderer.tesselate(level, pos, emitters, state, fluidState);
                }
            }

            if (state.getRenderShape() == RenderShape.MODEL) {
                blockRenderer.tesselateBlock(
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    level,
                    pos,
                    state,
                    blockStateModelSet.get(state),
                    state.getSeed(pos)
                );
            }
        }

        BlockModelLighter.clearCache();
        return emitters.end();
    }

    private static class ThreadLocalObjects {
        public final VanillinMeshEmitterManager emitters = new VanillinMeshEmitterManager();
        public final VanillinTransformMeshEmitterManager transformEmitters = new VanillinTransformMeshEmitterManager(
            emitters);
    }
}
