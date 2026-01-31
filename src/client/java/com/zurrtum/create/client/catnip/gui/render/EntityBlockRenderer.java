package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.BlitRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class EntityBlockRenderer extends PictureInPictureRenderer<EntityBlockRenderState> {
    private static final Int2ObjectMap<GpuTexture> TEXTURES = new Int2ObjectArrayMap<>();
    private static final CameraRenderState CAMERA = new CameraRenderState();
    private final PoseStack matrices = new PoseStack();
    private int windowScaleFactor;

    public EntityBlockRenderer(MultiBufferSource.BufferSource vertexConsumers) {
        super(vertexConsumers);
    }

    public static void clear(int key) {
        GpuTexture texture = TEXTURES.remove(key);
        if (texture != null) {
            texture.close();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void prepare(EntityBlockRenderState block, GuiRenderState state, int windowScaleFactor) {
        if (this.windowScaleFactor != windowScaleFactor) {
            this.windowScaleFactor = windowScaleFactor;
            TEXTURES.values().forEach(GpuTexture::close);
            TEXTURES.clear();
        }
        float size = block.size() * windowScaleFactor;
        GpuTexture texture = TEXTURES.get(block.id());
        if (texture == null) {
            texture = GpuTexture.create((int) size);
            TEXTURES.put(block.id(), texture);
        }
        RenderSystem.setProjectionMatrix(projectionMatrixBuffer.getBuffer(size, size), ProjectionType.ORTHOGRAPHIC);
        texture.prepare();
        matrices.pushPose();
        matrices.translate(size / 2, size / 2, 0);
        float scale = block.scale() * windowScaleFactor;
        matrices.scale(scale, -scale, scale);
        Minecraft mc = Minecraft.getInstance();
        GameRenderer gameRenderer = mc.gameRenderer;
        boolean lightOption = gameRenderer.useUiLightmap;
        gameRenderer.useUiLightmap = false;
        gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        if (block.zRot() != 0) {
            matrices.mulPose(Axis.ZP.rotation(block.zRot()));
        }
        if (block.xRot() != 0) {
            matrices.mulPose(Axis.XP.rotation(block.xRot()));
        }
        if (block.yRot() != 0) {
            matrices.mulPose(Axis.YP.rotation(block.yRot()));
        }
        matrices.translate(-0.5F, -0.5F, -0.5F);
        BlockRenderDispatcher blockRenderManager = mc.getBlockRenderer();
        Level world = block.world();
        BlockState blockState = block.state();
        BlockEntity blockEntity = block.entity();
        RenderType layer = ItemBlockRenderTypes.getChunkRenderType(blockState) == ChunkSectionLayer.TRANSLUCENT ? Sheets.translucentItemSheet() : Sheets.cutoutBlockSheet();
        SinglePosVirtualBlockGetter lightWorld = SinglePosVirtualBlockGetter.createFullBright();
        lightWorld.blockState(blockState);
        lightWorld.blockEntity(blockEntity);
        BlockStateModel model = blockRenderManager.getBlockModel(blockState);
        List<BlockModelPart> parts = new ObjectArrayList<>();
        RandomSource random = world.getRandom();
        if (WrapperBlockStateModel.unwrapCompat(model) instanceof WrapperBlockStateModel wrapper) {
            wrapper.addPartsWithInfo(world, block.pos(), blockState, random, parts);
        } else {
            model.collectParts(random, parts);
        }
        blockRenderManager.renderBatched(blockState, BlockPos.ZERO, lightWorld, matrices, bufferSource.getBuffer(layer), false, parts);
        if (blockEntity != null) {
            BlockEntityRenderer<BlockEntity, BlockEntityRenderState> renderer = mc.getBlockEntityRenderDispatcher().getRenderer(blockEntity);
            if (renderer != null) {
                FeatureRenderDispatcher renderDispatcher = gameRenderer.getFeatureRenderDispatcher();
                Level previousLevel = blockEntity.getLevel();
                BlockState stateBefore = blockEntity.getBlockState();
                blockEntity.setLevel(world);
                blockEntity.setBlockState(blockState);
                BlockEntityRenderState renderState = renderer.createRenderState();
                renderer.extractRenderState(blockEntity, renderState, 0, CAMERA.pos, null);
                renderer.submit(renderState, matrices, renderDispatcher.getSubmitNodeStorage(), CAMERA);
                renderDispatcher.renderAllFeatures();
                blockEntity.setBlockState(stateBefore);
                blockEntity.setLevel(previousLevel);
            }
        }
        bufferSource.endBatch();
        gameRenderer.useUiLightmap = lightOption;
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
    }

    @Override
    protected void renderToTexture(EntityBlockRenderState state, PoseStack matrices) {
    }

    @Override
    protected String getTextureLabel() {
        return "Entity Block";
    }

    @Override
    public Class<EntityBlockRenderState> getRenderStateClass() {
        return EntityBlockRenderState.class;
    }
}
