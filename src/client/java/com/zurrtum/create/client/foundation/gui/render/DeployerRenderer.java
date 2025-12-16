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
import com.zurrtum.create.content.kinetics.deployer.DeployerBlock;
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
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

public class DeployerRenderer extends PictureInPictureRenderer<DeployerRenderState> {
    private static final Int2ObjectMap<GpuTexture> TEXTURES = new Int2ObjectArrayMap<>();
    private final PoseStack matrices = new PoseStack();
    private int windowScaleFactor;

    public DeployerRenderer(MultiBufferSource.BufferSource vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    public void prepare(DeployerRenderState item, GuiRenderState state, int windowScaleFactor) {
        if (this.windowScaleFactor != windowScaleFactor) {
            this.windowScaleFactor = windowScaleFactor;
            TEXTURES.values().forEach(GpuTexture::close);
            TEXTURES.clear();
        }
        int width = 26 * windowScaleFactor;
        int height = 75 * windowScaleFactor;
        GpuTexture texture = TEXTURES.get(item.id());
        if (texture == null) {
            texture = GpuTexture.create(width, height);
            TEXTURES.put(item.id(), texture);
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
        matrices.translate(-0.5f, -2.24f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockState blockState;
        List<BlockModelPart> parts;
        BlockRenderDispatcher blockRenderManager = mc.getBlockRenderer();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        VertexConsumer buffer = bufferSource.getBuffer(Sheets.cutoutBlockSheet());
        float time = AnimationTickHolder.getRenderTime();
        float cycle = (time - item.offset() * 8) % 30;
        float offset = cycle < 10 ? cycle / 10f : cycle < 20 ? (20 - cycle) / 10f : 0;

        matrices.pushPose();
        blockState = AllBlocks.SHAFT.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.Z);
        world.blockState(blockState);
        parts = blockRenderManager.getBlockModel(blockState).collectParts(mc.level.getRandom());
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.mulPose(Axis.ZP.rotationDegrees(getCurrentAngle(time)));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderManager.renderBatched(blockState, BlockPos.ZERO, world, matrices, buffer, false, parts);
        matrices.popPose();

        blockState = AllBlocks.DEPLOYER.defaultBlockState().setValue(DeployerBlock.FACING, Direction.DOWN)
            .setValue(DeployerBlock.AXIS_ALONG_FIRST_COORDINATE, false);
        world.blockState(blockState);
        parts = blockRenderManager.getBlockModel(blockState).collectParts(mc.level.getRandom());
        blockRenderManager.renderBatched(blockState, BlockPos.ZERO, world, matrices, buffer, false, parts);

        matrices.pushPose();
        blockState = Blocks.AIR.defaultBlockState();
        world.blockState(blockState);
        parts = List.of(AllPartialModels.DEPLOYER_POLE.get(), AllPartialModels.DEPLOYER_HAND_HOLDING.get());
        matrices.translate(0, -offset, 0);
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.mulPose(Axis.XP.rotationDegrees(90));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        blockRenderManager.renderBatched(blockState, BlockPos.ZERO, world, matrices, buffer, false, parts);
        matrices.popPose();

        matrices.translate(0, -2.06f, 0);
        blockState = AllBlocks.DEPOT.defaultBlockState();
        world.blockState(blockState);
        parts = blockRenderManager.getBlockModel(blockState).collectParts(mc.level.getRandom());
        blockRenderManager.renderBatched(blockState, BlockPos.ZERO, world, matrices, buffer, false, parts);

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

    public static float getCurrentAngle(float time) {
        return (time * 4f) % 360;
    }

    @Override
    protected void renderToTexture(DeployerRenderState state, PoseStack matrices) {
    }

    @Override
    protected String getTextureLabel() {
        return "Deployer";
    }

    @Override
    public Class<DeployerRenderState> getRenderStateClass() {
        return DeployerRenderState.class;
    }
}
