package com.zurrtum.create.client.content.schematics.client;

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
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.command.RenderDispatcher;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.*;

public class SchematicRenderer {

    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

    private final Map<BlockRenderLayer, SuperByteBuffer> bufferCache = new LinkedHashMap<>(BlockRenderLayer.values().length);
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

    public void render(MinecraftClient mc, MatrixStack ms, SuperRenderTypeBuffer buffers, SchematicTransformation transformation, Vec3d camera) {
        if (mc.world == null || mc.player == null)
            return;
        if (changed)
            redraw(mc);
        changed = false;

        bufferCache.forEach((layer, buffer) -> {
            buffer.renderInto(ms.peek(), buffers.getBuffer(layer));
        });
        scratchErroredBlockEntities.clear();
        BlockEntityListRenderState renderState = BlockEntityRenderHelper.getBlockEntitiesRenderState(
            VisualizationManager.supportsVisualization(schematic),
            renderedBlockEntities,
            shouldRenderBlockEntities,
            scratchErroredBlockEntities,
            null,
            schematic,
            null,
            null,
            transformation.toLocalSpace(camera),
            AnimationTickHolder.getPartialTicks()
        );
        if (renderState != null) {
            RenderDispatcher renderDispatcher = MinecraftClient.getInstance().gameRenderer.getEntityRenderDispatcher();
            renderState.render(ms, renderDispatcher.getQueue(), mc.gameRenderer.getEntityRenderStates().cameraRenderState);
        }

        // Don't bother looping over errored BEs again.
        shouldRenderBlockEntities.andNot(scratchErroredBlockEntities);
    }

    protected void redraw(MinecraftClient mc) {
        bufferCache.clear();

        for (BlockRenderLayer layer : BlockRenderLayer.values()) {
            SuperByteBuffer buffer = drawLayer(mc, layer);
            if (!buffer.isEmpty())
                bufferCache.put(layer, buffer);
        }
    }

    @SuppressWarnings("removal")
    protected SuperByteBuffer drawLayer(MinecraftClient mc, BlockRenderLayer layer) {
        BlockRenderManager dispatcher = mc.getBlockRenderManager();
        BlockModelRenderer renderer = dispatcher.getModelRenderer();
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

        MatrixStack poseStack = objects.poseStack;
        Random random = objects.random;
        BlockPos.Mutable mutableBlockPos = objects.mutableBlockPos;
        SchematicLevel renderWorld = schematic;
        BlockBox bounds = renderWorld.getBounds();

        ShadedBlockSbbBuilder sbbBuilder = objects.sbbBuilder;
        sbbBuilder.begin();

        renderWorld.renderMode = true;
        BlockModelRenderer.enableBrightnessCache();
        for (BlockPos localPos : BlockPos.iterate(
            bounds.getMinX(),
            bounds.getMinY(),
            bounds.getMinZ(),
            bounds.getMaxX(),
            bounds.getMaxY(),
            bounds.getMaxZ()
        )) {
            BlockPos pos = mutableBlockPos.set(localPos, anchor);
            BlockState state = renderWorld.getBlockState(pos);

            if (state.getRenderType() == BlockRenderType.MODEL && RenderLayers.getBlockLayer(state) == layer) {
                long seed = state.getRenderingSeed(pos);
                BlockStateModel model = dispatcher.getModel(state);
                random.setSeed(seed);
                poseStack.push();
                poseStack.translate(localPos.getX(), localPos.getY(), localPos.getZ());
                List<BlockModelPart> parts = new ObjectArrayList<>();
                if (WrapperBlockStateModel.unwrapCompat(model) instanceof WrapperBlockStateModel wrapper) {
                    wrapper.addPartsWithInfo(renderWorld, pos, state, random, parts);
                } else {
                    model.addParts(random, parts);
                }
                renderer.render(renderWorld, parts, state, pos, poseStack, sbbBuilder, true, OverlayTexture.DEFAULT_UV);
                poseStack.pop();
            }
        }
        BlockModelRenderer.disableBrightnessCache();
        renderWorld.renderMode = false;

        return sbbBuilder.end();
    }

    private static class ThreadLocalObjects {
        public final MatrixStack poseStack = new MatrixStack();
        public final Random random = Random.createLocal();
        public final BlockPos.Mutable mutableBlockPos = new BlockPos.Mutable();
        @SuppressWarnings("removal")
        public final ShadedBlockSbbBuilder sbbBuilder = ShadedBlockSbbBuilder.create();
    }

}
