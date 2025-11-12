package com.zurrtum.create.client.content.schematics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.levelWrappers.SchematicLevel;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.ShadedBlockSbbBuilder;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.render.BlockEntityRenderHelper;
import com.zurrtum.create.client.foundation.render.BlockEntityRenderHelper.BlockEntityListRenderState;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;

public class SchematicRenderer {

    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

    private final Map<ChunkSectionLayer, SuperByteBuffer> bufferCache = new LinkedHashMap<>(ChunkSectionLayer.values().length);
    private boolean changed;
    protected final SchematicLevel schematic;
    private final BlockPos anchor;
    private final List<BlockEntity> renderedBlockEntities = new ArrayList<>();
    private final BitSet shouldRenderBlockEntities = new BitSet();
    private final BitSet scratchErroredBlockEntities = new BitSet();

    public SchematicRenderer(SchematicLevel world) {
        this.anchor = world.anchor;
        this.schematic = world;
        this.changed = true;

        for (var renderedBlockEntity : schematic.getRenderedBlockEntities()) {
            renderedBlockEntities.add(renderedBlockEntity);
        }
        shouldRenderBlockEntities.set(0, renderedBlockEntities.size());
    }

    public void update() {
        changed = true;
    }

    public void render(Minecraft mc, PoseStack ms, SuperRenderTypeBuffer buffers, SchematicTransformation transformation, Vec3 camera) {
        if (mc.level == null || mc.player == null)
            return;
        if (changed)
            redraw(mc);
        changed = false;

        bufferCache.forEach((layer, buffer) -> {
            buffer.renderInto(ms.last(), buffers.getBuffer(layer));
        });
        scratchErroredBlockEntities.clear();
        BlockEntityListRenderState renderState = BlockEntityRenderHelper.getBlockEntitiesRenderState(
            VisualizationManager.supportsVisualization(schematic),
            renderedBlockEntities,
            shouldRenderBlockEntities,
            scratchErroredBlockEntities,
            null,
            schematic,
            ms,
            null,
            transformation.toLocalSpace(camera),
            AnimationTickHolder.getPartialTicks()
        );
        if (renderState != null) {
            FeatureRenderDispatcher renderDispatcher = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
            renderState.render(renderDispatcher.getSubmitNodeStorage(), mc.gameRenderer.getLevelRenderState().cameraRenderState);
        }

        // Don't bother looping over errored BEs again.
        shouldRenderBlockEntities.andNot(scratchErroredBlockEntities);
    }

    protected void redraw(Minecraft mc) {
        bufferCache.clear();

        for (ChunkSectionLayer layer : ChunkSectionLayer.values()) {
            SuperByteBuffer buffer = drawLayer(mc, layer);
            if (!buffer.isEmpty())
                bufferCache.put(layer, buffer);
        }
    }

    @SuppressWarnings("removal")
    protected SuperByteBuffer drawLayer(Minecraft mc, ChunkSectionLayer layer) {
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        ModelBlockRenderer renderer = dispatcher.getModelRenderer();
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

        PoseStack poseStack = objects.poseStack;
        RandomSource random = objects.random;
        BlockPos.MutableBlockPos mutableBlockPos = objects.mutableBlockPos;
        SchematicLevel renderWorld = schematic;
        BoundingBox bounds = renderWorld.getBounds();

        ShadedBlockSbbBuilder sbbBuilder = objects.sbbBuilder;
        sbbBuilder.begin();

        renderWorld.renderMode = true;
        ModelBlockRenderer.enableCaching();
        for (BlockPos localPos : BlockPos.betweenClosed(
            bounds.minX(),
            bounds.minY(),
            bounds.minZ(),
            bounds.maxX(),
            bounds.maxY(),
            bounds.maxZ()
        )) {
            BlockPos pos = mutableBlockPos.setWithOffset(localPos, anchor);
            BlockState state = renderWorld.getBlockState(pos);

            if (state.getRenderShape() == RenderShape.MODEL && ItemBlockRenderTypes.getChunkRenderType(state) == layer) {
                long seed = state.getSeed(pos);
                BlockStateModel model = dispatcher.getBlockModel(state);
                random.setSeed(seed);
                poseStack.pushPose();
                poseStack.translate(localPos.getX(), localPos.getY(), localPos.getZ());
                List<BlockModelPart> parts = new ObjectArrayList<>();
                if (WrapperBlockStateModel.unwrapCompat(model) instanceof WrapperBlockStateModel wrapper) {
                    wrapper.addPartsWithInfo(renderWorld, pos, state, random, parts);
                } else {
                    model.collectParts(random, parts);
                }
                renderer.tesselateBlock(renderWorld, parts, state, pos, poseStack, sbbBuilder, true, OverlayTexture.NO_OVERLAY);
                poseStack.popPose();
            }
        }
        ModelBlockRenderer.clearCache();
        renderWorld.renderMode = false;

        return sbbBuilder.end();
    }

    private static class ThreadLocalObjects {
        public final PoseStack poseStack = new PoseStack();
        public final RandomSource random = RandomSource.createNewThreadLocalInstance();
        public final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        @SuppressWarnings("removal")
        public final ShadedBlockSbbBuilder sbbBuilder = ShadedBlockSbbBuilder.create();
    }

}
