package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.render.GpuTexture;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
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
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

public class SpoutRenderer extends PictureInPictureRenderer<SpoutRenderState> {
    private static final Int2ObjectMap<GpuTexture> TEXTURES = new Int2ObjectArrayMap<>();
    private final PoseStack matrices = new PoseStack();
    private int windowScaleFactor;

    public SpoutRenderer(MultiBufferSource.BufferSource vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    public void prepare(SpoutRenderState item, GuiRenderState state, int windowScaleFactor) {
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
        RenderSystem.setProjectionMatrix(projectionMatrixBuffer.getBuffer(width, height), ProjectionType.ORTHOGRAPHIC);
        texture.prepare();
        matrices.pushPose();
        matrices.translate(width / 2.0F, height / 2.0F, 0.0F);
        float scale = 20 * windowScaleFactor;
        matrices.scale(scale, scale, scale);

        Minecraft mc = Minecraft.getInstance();
        mc.gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        matrices.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrices.mulPose(Axis.YP.rotationDegrees(22.5f));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockState blockState;
        List<BlockModelPart> parts;
        BlockRenderDispatcher blockRenderManager = mc.getBlockRenderer();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        VertexConsumer buffer = bufferSource.getBuffer(Sheets.cutoutBlockSheet());
        float time = AnimationTickHolder.getRenderTime();

        blockState = AllBlocks.SPOUT.defaultBlockState();
        world.blockState(blockState);
        parts = blockRenderManager.getBlockModel(blockState).collectParts(mc.level.getRandom());
        blockRenderManager.renderBatched(blockState, BlockPos.ZERO, world, matrices, buffer, false, parts);

        float cycle = (time - item.offset() * 8) % 30;
        float squeeze = cycle < 20 ? -Mth.sin((float) (cycle / 20f * Math.PI)) : 0;
        float move = -3 * squeeze / 32f;

        blockState = Blocks.AIR.defaultBlockState();
        world.blockState(blockState);
        parts = List.of(AllPartialModels.SPOUT_TOP.get());
        blockRenderManager.renderBatched(blockState, BlockPos.ZERO, world, matrices, buffer, false, parts);
        matrices.pushPose();
        parts = List.of(AllPartialModels.SPOUT_MIDDLE.get());
        matrices.translate(0, move, 0);
        blockRenderManager.renderBatched(blockState, BlockPos.ZERO, world, matrices, buffer, false, parts);
        parts = List.of(AllPartialModels.SPOUT_BOTTOM.get());
        matrices.translate(0, move, 0);
        blockRenderManager.renderBatched(blockState, BlockPos.ZERO, world, matrices, buffer, false, parts);
        matrices.popPose();

        matrices.pushPose();
        blockState = AllBlocks.DEPOT.defaultBlockState();
        world.blockState(blockState);
        parts = blockRenderManager.getBlockModel(blockState).collectParts(mc.level.getRandom());
        matrices.translate(0.07f, -2, -0.14f);
        blockRenderManager.renderBatched(blockState, BlockPos.ZERO, world, matrices, buffer, false, parts);
        matrices.popPose();
        matrices.popPose();

        Fluid fluid = item.fluid();
        if (fluid != Fluids.EMPTY) {
            DataComponentPatch components = item.components();
            matrices.pushPose();
            matrices.mulPose(Axis.XP.rotationDegrees(-15.5f));
            matrices.mulPose(Axis.YP.rotationDegrees(22.5f));
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
                bufferSource,
                matrices,
                LightTexture.FULL_BRIGHT,
                false,
                true
            );
            matrices.popPose();

            matrices.pushPose();
            matrices.mulPose(Axis.XP.rotationDegrees(-15.5f));
            matrices.mulPose(Axis.YP.rotationDegrees(22.5f));
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
                bufferSource,
                matrices,
                LightTexture.FULL_BRIGHT,
                false,
                true
            );
            matrices.popPose();
        }

        bufferSource.endBatch();
        texture.clear();
        state.submitBlitToCurrentLayer(new BlitRenderState(
            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
            TextureSetup.singleTexture(texture.textureView(), RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST)),
            item.pose(),
            item.x0(),
            item.y0(),
            item.x1(),
            item.y1(),
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
    protected void renderToTexture(SpoutRenderState state, PoseStack matrices) {
    }

    @Override
    protected String getTextureLabel() {
        return "Spout";
    }

    @Override
    public Class<SpoutRenderState> getRenderStateClass() {
        return SpoutRenderState.class;
    }
}
