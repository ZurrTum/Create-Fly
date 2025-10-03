package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.render.GpuTexture;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
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
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.ComponentChanges;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.util.List;

public class SpoutRenderer extends SpecialGuiElementRenderer<SpoutRenderState> {
    private static final Int2ObjectMap<GpuTexture> TEXTURES = new Int2ObjectArrayMap<>();
    private final MatrixStack matrices = new MatrixStack();
    private int windowScaleFactor;

    public SpoutRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    public void render(SpoutRenderState item, GuiRenderState state, int windowScaleFactor) {
        if (this.windowScaleFactor != windowScaleFactor) {
            this.windowScaleFactor = windowScaleFactor;
            TEXTURES.values().forEach(GpuTexture::close);
            TEXTURES.clear();
        }
        int width = 26 * windowScaleFactor;
        int height = 65 * windowScaleFactor;
        GpuTexture texture = TEXTURES.get(item.id());
        if (texture == null) {
            texture = GpuTexture.create(width, height);
            TEXTURES.put(item.id(), texture);
        }
        RenderSystem.setProjectionMatrix(projectionMatrix.set(width, height), ProjectionType.ORTHOGRAPHIC);
        texture.prepare();
        matrices.push();
        matrices.translate(width / 2.0F, height / 2.0F, 0.0F);
        float scale = 20 * windowScaleFactor;
        matrices.scale(scale, scale, scale);

        MinecraftClient mc = MinecraftClient.getInstance();
        mc.gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ENTITY_IN_UI);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-15.5f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(22.5f));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockState blockState;
        List<BlockModelPart> parts;
        BlockRenderManager blockRenderManager = mc.getBlockRenderManager();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        VertexConsumer buffer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());
        float time = AnimationTickHolder.getRenderTime();

        blockState = AllBlocks.SPOUT.getDefaultState();
        world.blockState(blockState);
        parts = blockRenderManager.getModel(blockState).getParts(mc.world.random);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, parts);

        float cycle = (time - item.offset() * 8) % 30;
        float squeeze = cycle < 20 ? -MathHelper.sin((float) (cycle / 20f * Math.PI)) : 0;
        float move = -3 * squeeze / 32f;

        blockState = Blocks.AIR.getDefaultState();
        world.blockState(blockState);
        parts = List.of(AllPartialModels.SPOUT_TOP.get());
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, parts);
        matrices.push();
        parts = List.of(AllPartialModels.SPOUT_MIDDLE.get());
        matrices.translate(0, move, 0);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, parts);
        parts = List.of(AllPartialModels.SPOUT_BOTTOM.get());
        matrices.translate(0, move, 0);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, parts);
        matrices.pop();

        matrices.push();
        blockState = AllBlocks.DEPOT.getDefaultState();
        world.blockState(blockState);
        parts = blockRenderManager.getModel(blockState).getParts(mc.world.random);
        matrices.translate(0.07f, -2, -0.14f);
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, world, matrices, buffer, false, parts);
        matrices.pop();
        matrices.pop();

        Fluid fluid = item.fluid();
        if (fluid != Fluids.EMPTY) {
            ComponentChanges components = item.components();
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-15.5f));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(22.5f));
            float fluidScale = 16 * windowScaleFactor;
            matrices.scale(fluidScale, -fluidScale, fluidScale);
            matrices.translate(0, -1.4f, 0);
            float from = 3f / 16f;
            float to = 17f / 16f;
            FluidRenderHelper.renderFluidBox(
                fluid,
                components,
                from,
                from,
                from,
                to,
                to,
                to,
                vertexConsumers,
                matrices,
                LightmapTextureManager.MAX_LIGHT_COORDINATE,
                false,
                true
            );
            matrices.pop();

            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-15.5f));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(22.5f));
            matrices.translate(scale / 2f, scale * 1.5f, scale / 2f);
            matrices.scale(fluidScale, -fluidScale, fluidScale);
            matrices.translate(-0.5f, -1f, -0.5f);
            float fluidWidth = 1 / 128f * -squeeze * 16;
            from = -fluidWidth / 2 + 0.5f;
            to = fluidWidth / 2 + 0.5f;
            FluidRenderHelper.renderFluidBox(
                fluid,
                components,
                from,
                0,
                from,
                to,
                2,
                to,
                vertexConsumers,
                matrices,
                LightmapTextureManager.MAX_LIGHT_COORDINATE,
                false,
                true
            );
            matrices.pop();
        }

        vertexConsumers.draw();
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

    @Override
    protected void render(SpoutRenderState state, MatrixStack matrices) {
    }

    @Override
    protected String getName() {
        return "Spout";
    }

    @Override
    public Class<SpoutRenderState> getElementClass() {
        return SpoutRenderState.class;
    }
}
