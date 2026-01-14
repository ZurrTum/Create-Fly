package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.zurrtum.create.client.flywheel.lib.model.SimpleModel;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import com.zurrtum.create.client.model.LayerBakedModel;
import com.zurrtum.create.foundation.block.LightControlBlock;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.BufferBuilder;
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

import java.util.Iterator;
import java.util.List;

final class BakedModelBufferer {
    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

    private BakedModelBufferer() {
    }

    private static boolean isDark(BlockRenderView level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof LightControlBlock block) {
            return block.getLuminance(level, pos) == 0;
        }
        return state.getLuminance() == 0;
    }

    public static SimpleModel bufferModel(
        GeometryBakedModel model,
        BlockPos pos,
        BlockRenderView level,
        BlockState state,
        @Nullable MatrixStack poseStack,
        BlockMaterialFunction blockMaterialFunction
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        if (poseStack == null) {
            poseStack = objects.identityPoseStack;
        }
        MeshEmitterManager<VanillinMeshEmitter> emitters = objects.emitters;
        emitters.prepare(blockMaterialFunction);
        BlockModelRenderer blockRenderer = MinecraftClient.getInstance().getBlockRenderManager().getModelRenderer();
        BlockRenderLayer renderType = LayerBakedModel.getBlockRenderLayer(model, () -> RenderLayers.getBlockLayer(state));
        VanillinMeshEmitter emitter = emitters.getEmitter(renderType);
        emitter.prepareForModelLayer(MinecraftClient.isAmbientOcclusionEnabled() && model.useAmbientOcclusion() && isDark(level, pos, state));
        poseStack.push();
        blockRenderer.render(level, List.of(model), state, pos, poseStack, emitter, false, OverlayTexture.DEFAULT_UV);
        poseStack.pop();
        return emitters.end();
    }

    public static SimpleModel bufferModel(
        BlockStateModel model,
        BlockPos pos,
        BlockRenderView level,
        BlockState state,
        @Nullable MatrixStack poseStack,
        BlockMaterialFunction blockMaterialFunction
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        Random random = objects.random;
        random.setSeed(state.getRenderingSeed(pos));
        List<BlockModelPart> parts = model.getParts(random);
        int size = parts.size();
        if (size == 0) {
            return new SimpleModel(List.of());
        }
        if (poseStack == null) {
            poseStack = objects.identityPoseStack;
        }
        MeshEmitterManager<VanillinMeshEmitter> emitters = objects.emitters;
        emitters.prepare(blockMaterialFunction);
        BlockModelRenderer blockRenderer = MinecraftClient.getInstance().getBlockRenderManager().getModelRenderer();
        Supplier<BlockRenderLayer> defaultLayer = Suppliers.memoize(() -> RenderLayers.getBlockLayer(state));
        BlockRenderLayer firstLayer = LayerBakedModel.getBlockRenderLayer(parts.getFirst(), defaultLayer);
        boolean aoEnabled = MinecraftClient.isAmbientOcclusionEnabled();
        if (size == 1) {
            VanillinMeshEmitter emitter = emitters.getEmitter(firstLayer);
            emitter.prepareForModelLayer(aoEnabled && parts.getFirst().useAmbientOcclusion() && isDark(level, pos, state));
            poseStack.push();
            blockRenderer.render(level, parts, state, pos, poseStack, emitter, false, OverlayTexture.DEFAULT_UV);
            poseStack.pop();
        } else {
            BlockRenderLayer[] renderLayers = new BlockRenderLayer[size];
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
                poseStack.push();
                blockRenderer.render(level, parts, state, pos, poseStack, emitter, false, OverlayTexture.DEFAULT_UV);
                poseStack.pop();
            } else {
                renderLayers[0] = firstLayer;
                if (aoEnabled) {
                    aoEnabled = isDark(level, pos, state);
                }
                for (int i = 0; i < size; i++) {
                    BlockModelPart part = parts.get(i);
                    VanillinMeshEmitter emitter = emitters.getEmitter(renderLayers[i]);
                    emitter.prepareForModelLayer(aoEnabled && part.useAmbientOcclusion());
                    poseStack.push();
                    blockRenderer.render(level, List.of(part), state, pos, poseStack, emitter, false, OverlayTexture.DEFAULT_UV);
                    poseStack.pop();
                }
            }
        }
        return emitters.end();
    }

    public static SimpleModel bufferBlocks(
        Iterator<BlockPos> posIterator,
        BlockRenderView level,
        @Nullable MatrixStack poseStack,
        boolean renderFluids,
        BlockMaterialFunction blockMaterialFunction
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        if (poseStack == null) {
            poseStack = objects.identityPoseStack;
        }
        Random random = objects.random;
        MeshEmitterManager<VanillinMeshEmitter> emitters = objects.emitters;
        TransformingVertexConsumer transformingWrapper = objects.transformingWrapper;

        emitters.prepare(blockMaterialFunction);

        BlockRenderManager renderDispatcher = MinecraftClient.getInstance().getBlockRenderManager();

        BlockModelRenderer blockRenderer = renderDispatcher.getModelRenderer();
        BlockModelRenderer.enableBrightnessCache();

        boolean aoEnabled = MinecraftClient.isAmbientOcclusionEnabled();

        while (posIterator.hasNext()) {
            BlockPos pos = posIterator.next();
            BlockState state = level.getBlockState(pos);

            if (renderFluids) {
                FluidState fluidState = state.getFluidState();
                if (!fluidState.isEmpty()) {
                    BlockRenderLayer renderType = RenderLayers.getFluidLayer(fluidState);

                    BufferBuilder bufferBuilder = emitters.getBuffer(renderType, true, false);

                    if (bufferBuilder != null) {
                        transformingWrapper.prepare(bufferBuilder, poseStack);

                        poseStack.push();
                        poseStack.translate(pos.getX() - (pos.getX() & 0xF), pos.getY() - (pos.getY() & 0xF), pos.getZ() - (pos.getZ() & 0xF));
                        renderDispatcher.renderFluid(pos, level, transformingWrapper, state, fluidState);
                        poseStack.pop();
                    }
                }
            }

            if (state.getRenderType() == BlockRenderType.MODEL) {
                BlockStateModel model = renderDispatcher.getModel(state);
                random.setSeed(state.getRenderingSeed(pos));
                List<BlockModelPart> parts = new ObjectArrayList<>();
                if (WrapperBlockStateModel.unwrapCompat(model) instanceof WrapperBlockStateModel wrapper) {
                    wrapper.addPartsWithInfo(level, pos, state, random, parts);
                } else {
                    model.addParts(random, parts);
                }
                if (!parts.isEmpty()) {
                    BlockRenderLayer renderType = RenderLayers.getBlockLayer(state);
                    VanillinMeshEmitter emitter = emitters.getEmitter(renderType);
                    emitter.prepareForModelLayer(aoEnabled && parts.getFirst().useAmbientOcclusion());
                    poseStack.push();
                    poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                    blockRenderer.render(level, parts, state, pos, poseStack, emitter, true, OverlayTexture.DEFAULT_UV);
                    poseStack.pop();
                }
            }
        }

        BlockModelRenderer.disableBrightnessCache();
        transformingWrapper.clear();
        return emitters.end();
    }

    private static class ThreadLocalObjects {
        public final MatrixStack identityPoseStack = new MatrixStack();
        public final Random random = Random.createLocal();

        public final MeshEmitterManager<VanillinMeshEmitter> emitters = new MeshEmitterManager<>(VanillinMeshEmitter::new);
        public final TransformingVertexConsumer transformingWrapper = new TransformingVertexConsumer();
    }
}
