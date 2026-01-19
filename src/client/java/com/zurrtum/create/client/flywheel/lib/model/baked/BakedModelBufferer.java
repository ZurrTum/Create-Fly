package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.flywheel.lib.model.SimpleModel;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import com.zurrtum.create.client.model.LayerBakedModel;
import com.zurrtum.create.foundation.block.LightControlBlock;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

final class BakedModelBufferer {
    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

    private BakedModelBufferer() {
    }

    private static boolean isDark(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof LightControlBlock block) {
            return block.getLuminance(level, pos) == 0;
        }
        return state.getLightEmission() == 0;
    }

    public static SimpleModel bufferModel(
        SimpleModelWrapper model,
        BlockPos pos,
        BlockAndTintGetter level,
        BlockState state,
        @Nullable PoseStack poseStack,
        BlockMaterialFunction blockMaterialFunction
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        if (poseStack == null) {
            poseStack = objects.identityPoseStack;
        }
        MeshEmitterManager<VanillinMeshEmitter> emitters = objects.emitters;
        emitters.prepare(blockMaterialFunction);
        ModelBlockRenderer blockRenderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
        ChunkSectionLayer renderType = LayerBakedModel.getBlockRenderLayer(model, () -> ItemBlockRenderTypes.getChunkRenderType(state));
        VanillinMeshEmitter emitter = emitters.getEmitter(renderType);
        emitter.prepareForModelLayer(Minecraft.useAmbientOcclusion() && model.useAmbientOcclusion() && isDark(level, pos, state));
        poseStack.pushPose();
        blockRenderer.tesselateBlock(level, List.of(model), state, pos, poseStack, emitter, false, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        return emitters.end();
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
        RandomSource random = objects.random;
        random.setSeed(state.getSeed(pos));
        List<BlockModelPart> parts = model.collectParts(random);
        int size = parts.size();
        if (size == 0) {
            return new SimpleModel(List.of());
        }
        if (poseStack == null) {
            poseStack = objects.identityPoseStack;
        }
        MeshEmitterManager<VanillinMeshEmitter> emitters = objects.emitters;
        emitters.prepare(blockMaterialFunction);
        ModelBlockRenderer blockRenderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();

        Supplier<ChunkSectionLayer> defaultLayer = Suppliers.memoize(() -> ItemBlockRenderTypes.getChunkRenderType(state));
        ChunkSectionLayer firstLayer = LayerBakedModel.getBlockRenderLayer(parts.getFirst(), defaultLayer);
        boolean aoEnabled = Minecraft.useAmbientOcclusion();
        if (size == 1) {
            VanillinMeshEmitter emitter = emitters.getEmitter(firstLayer);
            emitter.prepareForModelLayer(aoEnabled && parts.getFirst().useAmbientOcclusion() && isDark(level, pos, state));
            poseStack.pushPose();
            blockRenderer.tesselateBlock(level, parts, state, pos, poseStack, emitter, false, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        } else {
            ChunkSectionLayer[] renderLayers = new ChunkSectionLayer[size];
            boolean simple = true;
            for (int i = 1; i < size; i++) {
                renderLayers[i] = LayerBakedModel.getBlockRenderLayer(parts.get(i), defaultLayer);
                if (simple && renderLayers[i] != firstLayer) {
                    simple = false;
                }
            }
            if (simple) {
                VanillinMeshEmitter emitter = emitters.getEmitter(firstLayer);
                emitter.prepareForModelLayer(aoEnabled && parts.getFirst().useAmbientOcclusion() && isDark(level, pos, state));
                poseStack.pushPose();
                blockRenderer.tesselateBlock(level, parts, state, pos, poseStack, emitter, false, OverlayTexture.NO_OVERLAY);
                poseStack.popPose();
            } else {
                renderLayers[0] = firstLayer;
                if (aoEnabled) {
                    aoEnabled = isDark(level, pos, state);
                }
                for (int i = 0; i < size; i++) {
                    BlockModelPart part = parts.get(i);
                    VanillinMeshEmitter emitter = emitters.getEmitter(renderLayers[i]);
                    emitter.prepareForModelLayer(aoEnabled && part.useAmbientOcclusion());
                    poseStack.pushPose();
                    blockRenderer.tesselateBlock(level, List.of(part), state, pos, poseStack, emitter, false, OverlayTexture.NO_OVERLAY);
                    poseStack.popPose();
                }
            }
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
        if (poseStack == null) {
            poseStack = objects.identityPoseStack;
        }
        RandomSource random = objects.random;
        MeshEmitterManager<VanillinMeshEmitter> emitters = objects.emitters;
        TransformingVertexConsumer transformingWrapper = objects.transformingWrapper;

        emitters.prepare(blockMaterialFunction);

        BlockRenderDispatcher renderDispatcher = Minecraft.getInstance().getBlockRenderer();

        ModelBlockRenderer blockRenderer = renderDispatcher.getModelRenderer();
        ModelBlockRenderer.enableCaching();

        boolean aoEnabled = Minecraft.useAmbientOcclusion();

        while (posIterator.hasNext()) {
            BlockPos pos = posIterator.next();
            BlockState state = level.getBlockState(pos);

            if (renderFluids) {
                FluidState fluidState = state.getFluidState();
                if (!fluidState.isEmpty()) {
                    ChunkSectionLayer renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);

                    BufferBuilder bufferBuilder = emitters.getBuffer(renderType, true, false);

                    if (bufferBuilder != null) {
                        transformingWrapper.prepare(bufferBuilder, poseStack);

                        poseStack.pushPose();
                        poseStack.translate(pos.getX() - (pos.getX() & 0xF), pos.getY() - (pos.getY() & 0xF), pos.getZ() - (pos.getZ() & 0xF));
                        renderDispatcher.renderLiquid(pos, level, transformingWrapper, state, fluidState);
                        poseStack.popPose();
                    }
                }
            }

            if (state.getRenderShape() == RenderShape.MODEL) {
                BlockStateModel model = renderDispatcher.getBlockModel(state);
                random.setSeed(state.getSeed(pos));
                List<BlockModelPart> parts = new ObjectArrayList<>();
                if (WrapperBlockStateModel.unwrapCompat(model) instanceof WrapperBlockStateModel wrapper) {
                    wrapper.addPartsWithInfo(level, pos, state, random, parts);
                } else {
                    model.collectParts(random, parts);
                }
                if (!parts.isEmpty()) {
                    ChunkSectionLayer renderType = ItemBlockRenderTypes.getChunkRenderType(state);
                    VanillinMeshEmitter emitter = emitters.getEmitter(renderType);
                    emitter.prepareForModelLayer(aoEnabled && parts.getFirst().useAmbientOcclusion());
                    poseStack.pushPose();
                    poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                    blockRenderer.tesselateBlock(level, parts, state, pos, poseStack, emitter, true, OverlayTexture.NO_OVERLAY);
                    poseStack.popPose();
                }
            }
        }

        ModelBlockRenderer.clearCache();
        transformingWrapper.clear();
        return emitters.end();
    }

    private static class ThreadLocalObjects {
        public final PoseStack identityPoseStack = new PoseStack();
        public final RandomSource random = RandomSource.createNewThreadLocalInstance();

        public final MeshEmitterManager<VanillinMeshEmitter> emitters = new MeshEmitterManager<>(VanillinMeshEmitter::new);
        public final TransformingVertexConsumer transformingWrapper = new TransformingVertexConsumer();
    }
}
