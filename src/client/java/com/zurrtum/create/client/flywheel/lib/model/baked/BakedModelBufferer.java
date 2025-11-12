package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import com.zurrtum.create.client.model.LayerBakedModel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

final class BakedModelBufferer {
    static final ChunkSectionLayer[] CHUNK_LAYERS = ChunkSectionLayer.values();
    static final Map<ChunkSectionLayer, Integer> CHUNK_LAYERS_INDEX = new HashMap<>();
    static final int CHUNK_LAYER_AMOUNT = CHUNK_LAYERS.length;

    static {
        for (int layerIndex = 0; layerIndex < CHUNK_LAYER_AMOUNT; layerIndex++) {
            CHUNK_LAYERS_INDEX.put(CHUNK_LAYERS[layerIndex], layerIndex);
        }
    }

    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

    private BakedModelBufferer() {
    }

    public static void bufferModel(
        SimpleModelWrapper model,
        BlockPos pos,
        BlockAndTintGetter level,
        BlockState state,
        @Nullable PoseStack poseStack,
        ResultConsumer resultConsumer
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        if (poseStack == null) {
            poseStack = objects.identityPoseStack;
        }
        MeshEmitter[] emitters = objects.emitters;
        ModelBlockRenderer blockRenderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
        ChunkSectionLayer renderType = LayerBakedModel.getBlockRenderLayer(model, () -> ItemBlockRenderTypes.getChunkRenderType(state));
        MeshEmitter emitter = emitters[CHUNK_LAYERS_INDEX.get(renderType)];

        emitter.prepare(resultConsumer);
        poseStack.pushPose();
        blockRenderer.tesselateBlock(level, List.of(model), state, pos, poseStack, emitter, false, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        emitter.end();
    }

    public static void bufferModel(
        BlockStateModel model,
        BlockPos pos,
        BlockAndTintGetter level,
        BlockState state,
        @Nullable PoseStack poseStack,
        ResultConsumer resultConsumer
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        if (poseStack == null) {
            poseStack = objects.identityPoseStack;
        }
        RandomSource random = objects.random;
        MeshEmitter[] emitters = objects.emitters;
        ModelBlockRenderer blockRenderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
        long seed = state.getSeed(pos);
        random.setSeed(seed);
        List<BlockModelPart> parts = model.collectParts(random);
        int size = parts.size();

        Supplier<ChunkSectionLayer> defaultLayer = Suppliers.memoize(() -> ItemBlockRenderTypes.getChunkRenderType(state));
        ChunkSectionLayer firstLayer = LayerBakedModel.getBlockRenderLayer(parts.getFirst(), defaultLayer);
        if (size == 1) {
            MeshEmitter emitter = emitters[CHUNK_LAYERS_INDEX.get(firstLayer)];
            emitter.prepare(resultConsumer);
            poseStack.pushPose();
            blockRenderer.tesselateBlock(level, parts, state, pos, poseStack, emitter, false, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
            emitter.end();
        } else {
            ChunkSectionLayer[] renderLayers = new ChunkSectionLayer[size];
            renderLayers[0] = firstLayer;
            boolean simple = true;
            for (int i = 1; i < size; i++) {
                renderLayers[i] = LayerBakedModel.getBlockRenderLayer(parts.get(i), defaultLayer);
                if (simple && renderLayers[i] != firstLayer) {
                    simple = false;
                }
            }
            if (simple) {
                MeshEmitter emitter = emitters[CHUNK_LAYERS_INDEX.get(firstLayer)];
                emitter.prepare(resultConsumer);
                poseStack.pushPose();
                blockRenderer.tesselateBlock(level, parts, state, pos, poseStack, emitter, false, OverlayTexture.NO_OVERLAY);
                poseStack.popPose();
                emitter.end();
            } else {
                boolean[] pending = new boolean[size];
                for (int i = 0; i < size; i++) {
                    int index = CHUNK_LAYERS_INDEX.get(renderLayers[i]);
                    MeshEmitter emitter = emitters[index];
                    if (!pending[index]) {
                        pending[index] = true;
                        emitter.prepare(resultConsumer);
                    }
                    poseStack.pushPose();
                    blockRenderer.tesselateBlock(level, List.of(parts.get(i)), state, pos, poseStack, emitter, false, OverlayTexture.NO_OVERLAY);
                    poseStack.popPose();
                }
                for (int i = 0; i < size; i++) {
                    if (pending[i]) {
                        emitters[i].end();
                    }
                }
            }
        }
    }

    public static void bufferBlocks(
        Iterator<BlockPos> posIterator,
        BlockAndTintGetter level,
        @Nullable PoseStack poseStack,
        boolean renderFluids,
        ResultConsumer resultConsumer
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        if (poseStack == null) {
            poseStack = objects.identityPoseStack;
        }
        RandomSource random = objects.random;
        MeshEmitter[] emitters = objects.emitters;
        TransformingVertexConsumer transformingWrapper = objects.transformingWrapper;

        for (MeshEmitter emitter : emitters) {
            emitter.prepare(resultConsumer);
        }

        BlockRenderDispatcher renderDispatcher = Minecraft.getInstance().getBlockRenderer();

        ModelBlockRenderer blockRenderer = renderDispatcher.getModelRenderer();
        ModelBlockRenderer.enableCaching();

        while (posIterator.hasNext()) {
            BlockPos pos = posIterator.next();
            BlockState state = level.getBlockState(pos);

            if (renderFluids) {
                FluidState fluidState = state.getFluidState();

                if (!fluidState.isEmpty()) {
                    ChunkSectionLayer renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);
                    transformingWrapper.prepare(emitters[CHUNK_LAYERS_INDEX.get(renderType)].unwrap(true), poseStack);

                    poseStack.pushPose();
                    poseStack.translate(pos.getX() - (pos.getX() & 0xF), pos.getY() - (pos.getY() & 0xF), pos.getZ() - (pos.getZ() & 0xF));
                    renderDispatcher.renderLiquid(pos, level, transformingWrapper, state, fluidState);
                    poseStack.popPose();
                }
            }

            if (state.getRenderShape() == RenderShape.MODEL) {
                long seed = state.getSeed(pos);
                BlockStateModel model = renderDispatcher.getBlockModel(state);
                random.setSeed(seed);
                ChunkSectionLayer renderType = ItemBlockRenderTypes.getChunkRenderType(state);
                int layerIndex = CHUNK_LAYERS_INDEX.get(renderType);
                poseStack.pushPose();
                poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                List<BlockModelPart> parts = new ObjectArrayList<>();
                if (WrapperBlockStateModel.unwrapCompat(model) instanceof WrapperBlockStateModel wrapper) {
                    wrapper.addPartsWithInfo(level, pos, state, random, parts);
                } else {
                    model.collectParts(random, parts);
                }
                blockRenderer.tesselateBlock(level, parts, state, pos, poseStack, emitters[layerIndex], true, OverlayTexture.NO_OVERLAY);
                poseStack.popPose();
            }
        }

        ModelBlockRenderer.clearCache();
        transformingWrapper.clear();

        for (MeshEmitter emitter : emitters) {
            emitter.end();
        }
    }

    public interface ResultConsumer {
        void accept(ChunkSectionLayer renderType, boolean shaded, MeshData data);
    }

    private static class ThreadLocalObjects {
        public final PoseStack identityPoseStack = new PoseStack();
        public final RandomSource random = RandomSource.createNewThreadLocalInstance();

        public final MeshEmitter[] emitters = new MeshEmitter[CHUNK_LAYER_AMOUNT];
        public final TransformingVertexConsumer transformingWrapper = new TransformingVertexConsumer();

        {
            for (int layerIndex = 0; layerIndex < CHUNK_LAYER_AMOUNT; layerIndex++) {
                ChunkSectionLayer renderType = CHUNK_LAYERS[layerIndex];
                emitters[layerIndex] = new MeshEmitter(renderType);
            }
        }
    }
}
