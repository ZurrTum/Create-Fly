package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.catnip.gui.render.GpuTexture;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.TexturedQuadGuiElementRenderState;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class DrainRenderer extends SpecialGuiElementRenderer<DrainRenderState> {
    public static int MAX = 6;
    private int allocate = MAX;
    private static final Deque<GpuTexture> TEXTURES = new ArrayDeque<>(MAX);
    private final MatrixStack matrices = new MatrixStack();
    private int windowScaleFactor;

    public DrainRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    public void render(DrainRenderState element, GuiRenderState state, int windowScaleFactor) {
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
        RenderSystem.setProjectionMatrix(projectionMatrix.set(width, height), ProjectionType.ORTHOGRAPHIC);
        texture.prepare();
        matrices.push();
        matrices.translate(width / 2.0F, height, 0.0F);
        float scale = 20 * windowScaleFactor;
        matrices.scale(scale, scale, scale);

        MinecraftClient mc = MinecraftClient.getInstance();
        mc.gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ENTITY_IN_UI);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-15.5f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(22.5f));
        matrices.scale(1, -1, 1);
        matrices.translate(-0.5f, 0.2f, -0.5f);

        BlockRenderManager blockRenderManager = mc.getBlockRenderManager();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        VertexConsumer buffer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());

        BlockState blockState = AllBlocks.ITEM_DRAIN.getDefaultState();
        world.blockState(blockState);
        List<BlockModelPart> parts = blockRenderManager.getModel(blockState).getParts(mc.world.random);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, parts);

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
            vertexConsumers,
            matrices,
            LightmapTextureManager.MAX_LIGHT_COORDINATE,
            false,
            true
        );

        vertexConsumers.draw();
        matrices.pop();
        texture.clear();
        state.addSimpleElementToCurrentLayer(new TexturedQuadGuiElementRenderState(
            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
            TextureSetup.withoutGlTexture(texture.textureView()),
            element.pose(),
            element.x1(),
            element.y1(),
            element.x2(),
            element.y2(),
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
    protected void render(DrainRenderState state, MatrixStack matrices) {
    }

    @Override
    protected String getName() {
        return "Drain";
    }

    @Override
    public Class<DrainRenderState> getElementClass() {
        return DrainRenderState.class;
    }
}
