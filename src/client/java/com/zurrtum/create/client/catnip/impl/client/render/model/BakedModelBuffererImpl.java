package com.zurrtum.create.client.catnip.impl.client.render.model;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.zurrtum.create.client.catnip.client.render.model.ShadeSeparatedBufferSource;
import com.zurrtum.create.client.catnip.client.render.model.ShadeSeparatedResultConsumer;
import com.zurrtum.create.client.catnip.impl.client.render.TransformingVertexConsumer;
import com.zurrtum.create.client.infrastructure.model.CopycatModel;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import com.zurrtum.create.client.model.LayerBakedModel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

// Modified from https://github.com/Engine-Room/Flywheel/blob/2f67f54c8898d91a48126c3c753eefa6cd224f84/forge/src/lib/java/dev/engine_room/flywheel/lib/model/baked/BakedModelBufferer.java
public final class BakedModelBuffererImpl {
    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

    private BakedModelBuffererImpl() {
    }


    public static void bufferModel(
        BlockStateModel model,
        BlockPos pos,
        BlockRenderView level,
        BlockState state,
        @Nullable MatrixStack poseStack,
        ShadeSeparatedBufferSource bufferSource
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        Random random = objects.random;
        random.setSeed(state.getRenderingSeed(pos));
        if (poseStack == null) {
            poseStack = objects.identityPoseStack;
        }
        List<BlockModelPart> parts = new ObjectArrayList<>();
        if (model instanceof CopycatModel copycatModel) {
            copycatModel.addPartsWithInfo(level, pos, state, random, parts);
        } else {
            model.addParts(random, parts);
        }
        bufferModel(parts, pos, level, state, poseStack, bufferSource, objects.universalEmitter);
    }

    private static void bufferModel(
        List<BlockModelPart> parts,
        BlockPos pos,
        BlockRenderView level,
        BlockState state,
        MatrixStack poseStack,
        ShadeSeparatedBufferSource bufferSource,
        UniversalMeshEmitter universalEmitter
    ) {
        BlockModelRenderer blockRenderer = MinecraftClient.getInstance().getBlockRenderManager().getModelRenderer();
        int size = parts.size();
        if (size == 0) {
            return;
        }

        Supplier<BlockRenderLayer> defaultLayer = Suppliers.memoize(() -> RenderLayers.getBlockLayer(state));
        BlockRenderLayer firstLayer = LayerBakedModel.getBlockRenderLayer(parts.getFirst(), defaultLayer);
        if (size == 1) {
            render(universalEmitter, bufferSource, firstLayer, poseStack, blockRenderer, level, parts, state, pos);
        } else {
            BlockRenderLayer[] renderLayers = new BlockRenderLayer[size];
            renderLayers[0] = firstLayer;
            boolean simple = true;
            for (int i = 1; i < size; i++) {
                renderLayers[i] = LayerBakedModel.getBlockRenderLayer(parts.get(i), defaultLayer);
                if (simple && renderLayers[i] != firstLayer) {
                    simple = false;
                }
            }
            if (simple) {
                render(universalEmitter, bufferSource, firstLayer, poseStack, blockRenderer, level, parts, state, pos);
            } else {
                for (int i = 0; i < size; i++) {
                    render(universalEmitter, bufferSource, renderLayers[i], poseStack, blockRenderer, level, List.of(parts.get(i)), state, pos);
                }
            }
        }

        universalEmitter.clear();
    }

    private static void render(
        UniversalMeshEmitter universalEmitter,
        ShadeSeparatedBufferSource bufferSource,
        BlockRenderLayer layer,
        MatrixStack poseStack,
        BlockModelRenderer blockRenderer,
        BlockRenderView level,
        List<BlockModelPart> parts,
        BlockState state,
        BlockPos pos
    ) {
        universalEmitter.prepare(bufferSource, layer);
        poseStack.push();
        blockRenderer.render(level, parts, state, pos, poseStack, universalEmitter, false, OverlayTexture.DEFAULT_UV);
        poseStack.pop();
    }

    public static void bufferModel(
        List<BlockModelPart> parts,
        BlockPos pos,
        BlockRenderView level,
        BlockState state,
        @Nullable MatrixStack poseStack,
        ShadeSeparatedResultConsumer resultConsumer
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        DefaultShadeSeparatedBufferSource bufferSource = objects.defaultBufferSource;
        bufferSource.prepare(resultConsumer);
        if (poseStack == null) {
            poseStack = objects.identityPoseStack;
        }
        bufferModel(parts, pos, level, state, poseStack, bufferSource, objects.universalEmitter);
        bufferSource.end();
    }

    public static void bufferModel(
        BlockStateModel model,
        BlockPos pos,
        BlockRenderView level,
        BlockState state,
        @Nullable MatrixStack poseStack,
        ShadeSeparatedResultConsumer resultConsumer
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        DefaultShadeSeparatedBufferSource bufferSource = objects.defaultBufferSource;
        bufferSource.prepare(resultConsumer);
        bufferModel(model, pos, level, state, poseStack, bufferSource);
        bufferSource.end();
    }

    public static void bufferBlocks(
        Iterator<BlockPos> posIterator,
        BlockRenderView level,
        @Nullable MatrixStack poseStack,
        boolean renderFluids,
        ShadeSeparatedBufferSource bufferSource
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        if (poseStack == null) {
            poseStack = objects.identityPoseStack;
        }
        Random random = objects.random;
        UniversalMeshEmitter universalEmitter = objects.universalEmitter;
        TransformingVertexConsumer transformingWrapper = objects.transformingWrapper;

        BlockRenderManager renderDispatcher = MinecraftClient.getInstance().getBlockRenderManager();

        BlockModelRenderer blockRenderer = renderDispatcher.getModelRenderer();
        BlockModelRenderer.enableBrightnessCache();

        while (posIterator.hasNext()) {
            BlockPos pos = posIterator.next();
            BlockState state = level.getBlockState(pos);

            if (renderFluids) {
                FluidState fluidState = state.getFluidState();

                if (!fluidState.isEmpty()) {
                    BlockRenderLayer renderType = RenderLayers.getFluidLayer(fluidState);

                    transformingWrapper.prepare(bufferSource.getBuffer(renderType, true), poseStack);

                    poseStack.push();
                    poseStack.translate(pos.getX() - (pos.getX() & 0xF), pos.getY() - (pos.getY() & 0xF), pos.getZ() - (pos.getZ() & 0xF));
                    renderDispatcher.renderFluid(pos, level, transformingWrapper, state, fluidState);
                    poseStack.pop();
                }
            }

            if (state.getRenderType() == BlockRenderType.MODEL) {
                long seed = state.getRenderingSeed(pos);
                BlockStateModel model = renderDispatcher.getModel(state);
                random.setSeed(seed);
                BlockRenderLayer renderType = RenderLayers.getBlockLayer(state);
                universalEmitter.prepare(bufferSource, renderType);
                poseStack.push();
                poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                List<BlockModelPart> parts = new ObjectArrayList<>();
                if (WrapperBlockStateModel.unwrapCompat(model) instanceof WrapperBlockStateModel wrapper) {
                    wrapper.addPartsWithInfo(level, pos, state, random, parts);
                } else {
                    model.addParts(random, parts);
                }
                blockRenderer.render(level, parts, state, pos, poseStack, universalEmitter, true, OverlayTexture.DEFAULT_UV);
                poseStack.pop();
            }
        }

        BlockModelRenderer.disableBrightnessCache();
        transformingWrapper.clear();
        universalEmitter.clear();
    }

    public static void bufferBlocks(
        Iterator<BlockPos> posIterator,
        BlockRenderView level,
        @Nullable MatrixStack poseStack,
        boolean renderFluids,
        ShadeSeparatedResultConsumer resultConsumer
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        DefaultShadeSeparatedBufferSource bufferSource = objects.defaultBufferSource;
        bufferSource.prepare(resultConsumer);
        bufferBlocks(posIterator, level, poseStack, renderFluids, bufferSource);
        bufferSource.end();
    }

    private static class ThreadLocalObjects {
        public final MatrixStack identityPoseStack = new MatrixStack();
        public final Random random = Random.createLocal();

        public final DefaultShadeSeparatedBufferSource defaultBufferSource = new DefaultShadeSeparatedBufferSource();
        public final UniversalMeshEmitter universalEmitter = new UniversalMeshEmitter();
        public final TransformingVertexConsumer transformingWrapper = new TransformingVertexConsumer();
    }
}
