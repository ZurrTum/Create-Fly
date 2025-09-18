package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.render.GpuTexture;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.TexturedQuadGuiElementRenderState;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.RotationAxis;

import java.util.List;

public class PressRenderer extends SpecialGuiElementRenderer<PressRenderState> {
    private static final Int2ObjectMap<GpuTexture> TEXTURES = new Int2ObjectArrayMap<>();
    private final MatrixStack matrices = new MatrixStack();
    private int windowScaleFactor;

    public PressRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    public void render(PressRenderState item, GuiRenderState state, int windowScaleFactor) {
        if (this.windowScaleFactor != windowScaleFactor) {
            this.windowScaleFactor = windowScaleFactor;
            TEXTURES.values().forEach(GpuTexture::close);
            TEXTURES.clear();
        }
        int width = 30 * windowScaleFactor;
        int height = 64 * windowScaleFactor;
        GpuTexture texture = TEXTURES.get(item.id());
        if (texture == null) {
            texture = GpuTexture.create(width, height);
            TEXTURES.put(item.id(), texture);
        }
        RenderSystem.setProjectionMatrix(projectionMatrix.set(width, height), ProjectionType.ORTHOGRAPHIC);
        texture.prepare();
        matrices.push();
        matrices.translate(width / 2.0F, height, 0.0F);
        float scale = 23 * windowScaleFactor;
        matrices.scale(scale, scale, scale);

        MinecraftClient mc = MinecraftClient.getInstance();
        mc.gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ENTITY_IN_UI);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-15.5f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(22.5f));
        matrices.translate(-0.5f, -1.14f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockState blockState;
        List<BlockModelPart> parts;
        BlockRenderManager blockRenderManager = mc.getBlockRenderManager();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        VertexConsumer buffer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());
        float time = AnimationTickHolder.getRenderTime();

        blockState = AllBlocks.MECHANICAL_PRESS.getDefaultState();
        world.blockState(blockState);
        parts = blockRenderManager.getModel(blockState).getParts(mc.world.random);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, parts);

        matrices.push();
        blockState = AllBlocks.SHAFT.getDefaultState().with(Properties.AXIS, Axis.Z);
        world.blockState(blockState);
        parts = blockRenderManager.getModel(blockState).getParts(mc.world.random);
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(getShaftAngle(time)));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, parts);
        matrices.pop();

        matrices.push();
        blockState = Blocks.AIR.getDefaultState();
        world.blockState(blockState);
        parts = List.of(AllPartialModels.MECHANICAL_PRESS_HEAD.get());
        matrices.translate(0, getAnimatedHeadOffset(time, item.offset()), 0);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, parts);
        matrices.pop();

        vertexConsumers.draw();
        matrices.pop();
        texture.clear();
        state.addSimpleElementToCurrentLayer(new TexturedQuadGuiElementRenderState(
            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
            TextureSetup.withoutGlTexture(texture.textureView()),
            item.pose(),
            item.x1(),
            item.y1(),
            item.x2(),
            item.y2(),
            0.0F,
            1.0F,
            1.0F,
            0.0F,
            -1,
            null,
            null
        ));
    }

    private static float getShaftAngle(float time) {
        return (time * 4f) % 360;
    }

    private static float getAnimatedHeadOffset(float time, float offset) {
        float cycle = (time - offset * 8) % 30;
        if (cycle < 10) {
            float progress = cycle / 10;
            return -(progress * progress * progress);
        }
        if (cycle < 15)
            return -1;
        if (cycle < 20)
            return -1 + (1 - ((20 - cycle) / 5));
        return 0;
    }

    @Override
    protected void render(PressRenderState state, MatrixStack matrices) {
    }

    @Override
    protected String getName() {
        return "Press";
    }

    @Override
    public Class<PressRenderState> getElementClass() {
        return PressRenderState.class;
    }
}