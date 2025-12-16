package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.catnip.gui.render.GpuTexture;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.BlitRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class DrainRenderer extends PictureInPictureRenderer<DrainRenderState> {
    public static int MAX = 6;
    private int allocate = MAX;
    private static final Deque<GpuTexture> TEXTURES = new ArrayDeque<>(MAX);
    private final PoseStack matrices = new PoseStack();
    private int windowScaleFactor;

    public DrainRenderer(MultiBufferSource.BufferSource vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    public void prepare(DrainRenderState element, GuiRenderState state, int windowScaleFactor) {
        if (this.windowScaleFactor != windowScaleFactor) {
            this.windowScaleFactor = windowScaleFactor;
            TEXTURES.forEach(GpuTexture::close);
            TEXTURES.clear();
            allocate = MAX;
        }
        int width = 26 * windowScaleFactor;
        int height = 23 * windowScaleFactor;
        GpuTexture texture;
        if (allocate > 0) {
            allocate--;
            texture = GpuTexture.create(width, height);
        } else {
            texture = TEXTURES.poll();
            assert texture != null;
        }
        RenderSystem.setProjectionMatrix(projectionMatrixBuffer.getBuffer(width, height), ProjectionType.ORTHOGRAPHIC);
        texture.prepare();
        matrices.pushPose();
        matrices.translate(width / 2.0F, height, 0.0F);
        float scale = 20 * windowScaleFactor;
        matrices.scale(scale, scale, scale);

        Minecraft mc = Minecraft.getInstance();
        mc.gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        matrices.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrices.mulPose(Axis.YP.rotationDegrees(22.5f));
        matrices.scale(1, -1, 1);
        matrices.translate(-0.5f, 0.2f, -0.5f);

        BlockRenderDispatcher blockRenderManager = mc.getBlockRenderer();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        VertexConsumer buffer = bufferSource.getBuffer(Sheets.cutoutBlockSheet());

        BlockState blockState = AllBlocks.ITEM_DRAIN.defaultBlockState();
        world.blockState(blockState);
        List<BlockModelPart> parts = blockRenderManager.getBlockModel(blockState).collectParts(mc.level.getRandom());
        blockRenderManager.renderBatched(blockState, BlockPos.ZERO, world, matrices, buffer, false, parts);

        float from = 2 / 16f;
        float to = 1f - from;
        FluidRenderHelper.renderFluidBox(
            element.fluid(),
            element.components(),
            from,
            from,
            from,
            to,
            3 / 4f,
            to,
            bufferSource,
            matrices,
            LightTexture.FULL_BRIGHT,
            false,
            true
        );

        bufferSource.endBatch();
        matrices.popPose();
        texture.clear();
        state.submitBlitToCurrentLayer(new BlitRenderState(
            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
            TextureSetup.singleTexture(texture.textureView(), RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST)),
            element.pose(),
            element.x0(),
            element.y0(),
            element.x1(),
            element.y1(),
            0.0F,
            1.0F,
            1.0F,
            0.0F,
            -1,
            null,
            null
        ));
        TEXTURES.add(texture);
    }

    @Override
    protected void renderToTexture(DrainRenderState state, PoseStack matrices) {
    }

    @Override
    protected String getTextureLabel() {
        return "Drain";
    }

    @Override
    public Class<DrainRenderState> getRenderStateClass() {
        return DrainRenderState.class;
    }
}
