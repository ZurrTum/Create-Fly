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
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

public class PressRenderer extends PictureInPictureRenderer<PressRenderState> {
    private static final Int2ObjectMap<GpuTexture> TEXTURES = new Int2ObjectArrayMap<>();
    private final PoseStack matrices = new PoseStack();
    private int windowScaleFactor;

    public PressRenderer(MultiBufferSource.BufferSource vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    public void prepare(PressRenderState item, GuiRenderState state, int windowScaleFactor) {
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
        RenderSystem.setProjectionMatrix(projectionMatrixBuffer.getBuffer(width, height), ProjectionType.ORTHOGRAPHIC);
        texture.prepare();
        matrices.pushPose();
        matrices.translate(width / 2.0F, height, 0.0F);
        float scale = 23 * windowScaleFactor;
        matrices.scale(scale, scale, scale);

        Minecraft mc = Minecraft.getInstance();
        mc.gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        matrices.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrices.mulPose(Axis.YP.rotationDegrees(22.5f));
        matrices.translate(-0.5f, -1.14f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockState blockState;
        List<BlockModelPart> parts;
        BlockRenderDispatcher blockRenderManager = mc.getBlockRenderer();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        VertexConsumer buffer = bufferSource.getBuffer(Sheets.cutoutBlockSheet());
        float time = AnimationTickHolder.getRenderTime();

        blockState = AllBlocks.MECHANICAL_PRESS.defaultBlockState();
        world.blockState(blockState);
        parts = blockRenderManager.getBlockModel(blockState).collectParts(mc.level.random);
        blockRenderManager.renderBatched(blockState, BlockPos.ZERO, world, matrices, buffer, false, parts);

        matrices.pushPose();
        blockState = AllBlocks.SHAFT.defaultBlockState().setValue(BlockStateProperties.AXIS, net.minecraft.core.Direction.Axis.Z);
        world.blockState(blockState);
        parts = blockRenderManager.getBlockModel(blockState).collectParts(mc.level.random);
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.mulPose(Axis.ZP.rotationDegrees(getShaftAngle(time)));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderManager.renderBatched(blockState, BlockPos.ZERO, world, matrices, buffer, false, parts);
        matrices.popPose();

        matrices.pushPose();
        blockState = Blocks.AIR.defaultBlockState();
        world.blockState(blockState);
        parts = List.of(AllPartialModels.MECHANICAL_PRESS_HEAD.get());
        matrices.translate(0, getAnimatedHeadOffset(time, item.offset()), 0);
        blockRenderManager.renderBatched(blockState, BlockPos.ZERO, world, matrices, buffer, false, parts);
        matrices.popPose();

        bufferSource.endBatch();
        matrices.popPose();
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
    protected void renderToTexture(PressRenderState state, PoseStack matrices) {
    }

    @Override
    protected String getTextureLabel() {
        return "Press";
    }

    @Override
    public Class<PressRenderState> getRenderStateClass() {
        return PressRenderState.class;
    }
}