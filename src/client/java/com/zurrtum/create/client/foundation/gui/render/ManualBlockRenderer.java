package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.client.catnip.gui.render.GpuTexture;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.BlitRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class ManualBlockRenderer extends PictureInPictureRenderer<ManualBlockRenderState> {
    public static int MAX = 6;
    private int allocate = MAX;
    private static final Deque<GpuTexture> TEXTURES = new ArrayDeque<>(MAX);
    private static final RandomSource random = RandomSource.create();
    private final PoseStack matrices = new PoseStack();
    private int windowScaleFactor;

    public ManualBlockRenderer(MultiBufferSource.BufferSource vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    public void prepare(ManualBlockRenderState block, GuiRenderState state, int windowScaleFactor) {
        if (this.windowScaleFactor != windowScaleFactor) {
            this.windowScaleFactor = windowScaleFactor;
            TEXTURES.forEach(GpuTexture::close);
            TEXTURES.clear();
            allocate = MAX;
        }
        int size = 27 * windowScaleFactor;
        GpuTexture texture;
        if (allocate > 0) {
            allocate--;
            texture = GpuTexture.create(size);
        } else {
            texture = TEXTURES.poll();
            assert texture != null;
        }
        RenderSystem.setProjectionMatrix(projectionMatrixBuffer.getBuffer(size, size), ProjectionType.ORTHOGRAPHIC);
        texture.prepare();
        matrices.pushPose();
        matrices.translate(size / 2.0F, size, 0.0F);
        float scale = 20 * windowScaleFactor;
        matrices.scale(scale, scale, scale);

        Minecraft mc = Minecraft.getInstance();
        mc.gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        matrices.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrices.mulPose(Axis.YP.rotationDegrees(22.5f));
        matrices.translate(-0.5f, -0.2f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockRenderDispatcher blockRenderManager = mc.getBlockRenderer();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        VertexConsumer buffer = bufferSource.getBuffer(Sheets.cutoutBlockSheet());
        world.blockState(block.state());
        random.setSeed(42L);
        List<BlockModelPart> parts = blockRenderManager.getBlockModel(block.state()).collectParts(random);
        blockRenderManager.renderBatched(block.state(), BlockPos.ZERO, world, matrices, buffer, false, parts);

        bufferSource.endBatch();
        matrices.popPose();
        texture.clear();
        state.submitBlitToCurrentLayer(new BlitRenderState(
            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
            TextureSetup.singleTexture(texture.textureView(), RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST)),
            block.pose(),
            block.x0(),
            block.y0(),
            block.x1(),
            block.y1(),
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
    protected void renderToTexture(ManualBlockRenderState state, PoseStack matrices) {
    }

    @Override
    protected String getTextureLabel() {
        return "Manual Block";
    }

    @Override
    public Class<ManualBlockRenderState> getRenderStateClass() {
        return ManualBlockRenderState.class;
    }
}
