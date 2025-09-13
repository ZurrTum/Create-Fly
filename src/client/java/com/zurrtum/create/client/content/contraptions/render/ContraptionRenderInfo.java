package com.zurrtum.create.client.content.contraptions.render;

import com.zurrtum.create.client.catnip.render.ShadedBlockSbbBuilder;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferCache;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.Contraption.RenderedBlocks;
import com.zurrtum.create.content.contraptions.ContraptionWorld;
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
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class ContraptionRenderInfo {
    public static final SuperByteBufferCache.Compartment<Pair<Contraption, BlockRenderLayer>> CONTRAPTION = new SuperByteBufferCache.Compartment<>();
    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

    private final Contraption contraption;
    private final VirtualRenderWorld renderWorld;
    private final ContraptionMatrices matrices = new ContraptionMatrices();

    ContraptionRenderInfo(World level, Contraption contraption) {
        this.contraption = contraption;
        this.renderWorld = setupRenderWorld(level, contraption);
    }

    public static ContraptionRenderInfo get(Contraption contraption) {
        return ContraptionRenderInfoManager.MANAGERS.get(contraption.entity.getWorld()).getRenderInfo(contraption);
    }

    /**
     * Reset a contraption's renderer.
     *
     * @param contraption The contraption to invalidate.
     * @return true if there was a renderer associated with the given contraption.
     */
    public static boolean invalidate(Contraption contraption) {
        return ContraptionRenderInfoManager.MANAGERS.get(contraption.entity.getWorld()).invalidate(contraption);
    }

    public boolean isDead() {
        return !contraption.entity.isAliveOrStale();
    }

    public Contraption getContraption() {
        return contraption;
    }

    public VirtualRenderWorld getRenderWorld() {
        return renderWorld;
    }

    public ContraptionMatrices getMatrices() {
        return matrices;
    }

    public SuperByteBuffer getBuffer(BlockRenderLayer renderType) {
        return SuperByteBufferCache.getInstance().get(CONTRAPTION, Pair.of(contraption, renderType), () -> buildStructureBuffer(renderType));
    }

    public void invalidate() {
        for (BlockRenderLayer renderType : BlockRenderLayer.values()) {
            SuperByteBufferCache.getInstance().invalidate(CONTRAPTION, Pair.of(contraption, renderType));
        }
    }

    public static VirtualRenderWorld setupRenderWorld(World level, Contraption c) {
        ContraptionWorld contraptionWorld = c.getContraptionWorld();

        BlockPos origin = c.anchor;
        int minBuildHeight = contraptionWorld.getBottomY();
        int height = contraptionWorld.getHeight();
        VirtualRenderWorld renderWorld = new VirtualRenderWorld(level, minBuildHeight, height, origin) {
            @Override
            public boolean supportsVisualization() {
                return VisualizationManager.supportsVisualization(level);
            }
        };

        renderWorld.setBlockEntities(c.presentBlockEntities.values());
        for (StructureTemplate.StructureBlockInfo info : c.getBlocks().values())
            renderWorld.setBlockState(info.pos(), info.state(), 0);

        renderWorld.runLightEngine();
        return renderWorld;
    }

    @SuppressWarnings("removal")
    private SuperByteBuffer buildStructureBuffer(BlockRenderLayer layer) {
        BlockRenderManager dispatcher = MinecraftClient.getInstance().getBlockRenderManager();
        BlockModelRenderer renderer = dispatcher.getModelRenderer();
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

        MatrixStack poseStack = objects.poseStack;
        Random random = objects.random;
        RenderedBlocks blocks = contraption.getRenderedBlocks();

        ShadedBlockSbbBuilder sbbBuilder = objects.sbbBuilder;
        sbbBuilder.begin();

        BlockModelRenderer.enableBrightnessCache();
        for (BlockPos pos : blocks.positions()) {
            BlockState state = blocks.lookup().apply(pos);
            if (state.getRenderType() == BlockRenderType.MODEL) {
                BlockStateModel model = dispatcher.getModel(state);
                long randomSeed = state.getRenderingSeed(pos);
                random.setSeed(randomSeed);
                if (RenderLayers.getBlockLayer(state) == layer) {
                    poseStack.push();
                    poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                    List<BlockModelPart> parts = model.getParts(random);
                    renderer.render(renderWorld, parts, state, pos, poseStack, sbbBuilder, true, OverlayTexture.DEFAULT_UV);
                    poseStack.pop();
                }
            }
        }
        BlockModelRenderer.disableBrightnessCache();

        return sbbBuilder.end();
    }

    @SuppressWarnings("removal")
    private static class ThreadLocalObjects {
        public final MatrixStack poseStack = new MatrixStack();
        public final Random random = Random.createLocal();
        public final ShadedBlockSbbBuilder sbbBuilder = ShadedBlockSbbBuilder.create();
    }
}
