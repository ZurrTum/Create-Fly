package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import com.zurrtum.create.client.model.LayerBakedModel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.model.GeometryBakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

final class BakedModelBufferer {
    static final BlockRenderLayer[] CHUNK_LAYERS = BlockRenderLayer.values();
    static final Map<BlockRenderLayer, Integer> CHUNK_LAYERS_INDEX = new HashMap<>();
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
        GeometryBakedModel model,
        BlockPos pos,
        BlockRenderView level,
        BlockState state,
        @Nullable MatrixStack poseStack,
        ResultConsumer resultConsumer
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        if (poseStack == null) {
            poseStack = objects.identityPoseStack;
        }
        MeshEmitter[] emitters = objects.emitters;
        BlockModelRenderer blockRenderer = MinecraftClient.getInstance().getBlockRenderManager().getModelRenderer();
        BlockRenderLayer renderType = LayerBakedModel.getBlockRenderLayer(model, () -> RenderLayers.getBlockLayer(state));
        MeshEmitter emitter = emitters[CHUNK_LAYERS_INDEX.get(renderType)];

        emitter.prepare(resultConsumer);
        poseStack.push();
        blockRenderer.render(level, List.of(model), state, pos, poseStack, emitter, false, OverlayTexture.DEFAULT_UV);
        poseStack.pop();

        emitter.end();
    }

    public static void bufferModel(
        BlockStateModel model,
        BlockPos pos,
        BlockRenderView level,
        BlockState state,
        @Nullable MatrixStack poseStack,
        ResultConsumer resultConsumer
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        if (poseStack == null) {
            poseStack = objects.identityPoseStack;
        }
        Random random = objects.random;
        MeshEmitter[] emitters = objects.emitters;
        BlockModelRenderer blockRenderer = MinecraftClient.getInstance().getBlockRenderManager().getModelRenderer();
        long seed = state.getRenderingSeed(pos);
        random.setSeed(seed);
        List<BlockModelPart> parts = model.getParts(random);
        int size = parts.size();

        Supplier<BlockRenderLayer> defaultLayer = Suppliers.memoize(() -> RenderLayers.getBlockLayer(state));
        BlockRenderLayer firstLayer = LayerBakedModel.getBlockRenderLayer(parts.getFirst(), defaultLayer);
        if (size == 1) {
            MeshEmitter emitter = emitters[CHUNK_LAYERS_INDEX.get(firstLayer)];
            emitter.prepare(resultConsumer);
            poseStack.push();
            blockRenderer.render(level, parts, state, pos, poseStack, emitter, false, OverlayTexture.DEFAULT_UV);
            poseStack.pop();
            emitter.end();
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
                MeshEmitter emitter = emitters[CHUNK_LAYERS_INDEX.get(firstLayer)];
                emitter.prepare(resultConsumer);
                poseStack.push();
                blockRenderer.render(level, parts, state, pos, poseStack, emitter, false, OverlayTexture.DEFAULT_UV);
                poseStack.pop();
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
                    poseStack.push();
                    blockRenderer.render(level, List.of(parts.get(i)), state, pos, poseStack, emitter, false, OverlayTexture.DEFAULT_UV);
                    poseStack.pop();
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
        BlockRenderView level,
        @Nullable MatrixStack poseStack,
        boolean renderFluids,
        ResultConsumer resultConsumer
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        if (poseStack == null) {
            poseStack = objects.identityPoseStack;
        }
        Random random = objects.random;
        MeshEmitter[] emitters = objects.emitters;
        TransformingVertexConsumer transformingWrapper = objects.transformingWrapper;

        for (MeshEmitter emitter : emitters) {
            emitter.prepare(resultConsumer);
        }

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
                    transformingWrapper.prepare(emitters[CHUNK_LAYERS_INDEX.get(renderType)].unwrap(true), poseStack);

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
                int layerIndex = CHUNK_LAYERS_INDEX.get(renderType);
                poseStack.push();
                poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                List<BlockModelPart> parts = new ObjectArrayList<>();
                if (model instanceof WrapperBlockStateModel wrapper) {
                    wrapper.addPartsWithInfo(level, pos, state, random, parts);
                } else {
                    model.addParts(random, parts);
                }
                blockRenderer.render(level, parts, state, pos, poseStack, emitters[layerIndex], true, OverlayTexture.DEFAULT_UV);
                poseStack.pop();
            }
        }

        BlockModelRenderer.disableBrightnessCache();
        transformingWrapper.clear();

        for (MeshEmitter emitter : emitters) {
            emitter.end();
        }
    }

    public interface ResultConsumer {
        void accept(BlockRenderLayer renderType, boolean shaded, BuiltBuffer data);
    }

    private static class ThreadLocalObjects {
        public final MatrixStack identityPoseStack = new MatrixStack();
        public final Random random = Random.createLocal();

        public final MeshEmitter[] emitters = new MeshEmitter[CHUNK_LAYER_AMOUNT];
        public final TransformingVertexConsumer transformingWrapper = new TransformingVertexConsumer();

        {
            for (int layerIndex = 0; layerIndex < CHUNK_LAYER_AMOUNT; layerIndex++) {
                BlockRenderLayer renderType = CHUNK_LAYERS[layerIndex];
                emitters[layerIndex] = new MeshEmitter(renderType);
            }
        }
    }
}
