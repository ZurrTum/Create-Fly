package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.platform.Lighting.Entry;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class EntityBlockRenderer extends PictureInPictureRenderer<EntityBlockRenderState> {
    private static final Int2ObjectMap<GpuTexture> TEXTURES = new Int2ObjectArrayMap<>();
    private final CameraRenderState camera = new CameraRenderState();
    private final PoseStack matrices = new PoseStack();
    private int windowScaleFactor;

    public static void clear(int key) {
        GpuTexture texture = TEXTURES.remove(key);
        if (texture != null) {
            texture.close();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void prepare(
        EntityBlockRenderState block,
        GuiRenderState state,
        FeatureRenderDispatcher featureRenderDispatcher,
        int windowScaleFactor
    ) {
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
        texture.prepare(projection, projectionMatrixBuffer);
        matrices.pushPose();
        matrices.translate(size / 2, size / 2, 0);
        float scale = block.scale() * windowScaleFactor;
        matrices.scale(scale, -scale, scale);
        Minecraft mc = Minecraft.getInstance();
        GameRenderer gameRenderer = mc.gameRenderer;
        boolean lightOption = gameRenderer.useUiLightmap;
        gameRenderer.useUiLightmap = false;
        gameRenderer.lighting().setupFor(Entry.ENTITY_IN_UI);
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
        Level world = block.world();
        BlockState blockState = block.state();
        BlockEntity blockEntity = block.entity();
        CachedBuffers.block(blockState).light(block.light()).submit(matrices, submitNodeStorage);
        if (blockEntity != null) {
            BlockEntityRenderer<BlockEntity, BlockEntityRenderState> renderer = mc.getBlockEntityRenderDispatcher()
                .getRenderer(blockEntity);
            if (renderer != null) {
                BlockPos pos = blockEntity.getBlockPos();
                Vec3 cameraPos = camera.pos = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                if (renderer.shouldRender(blockEntity, cameraPos)) {
                    Level previousLevel = blockEntity.getLevel();
                    BlockState stateBefore = blockEntity.getBlockState();
                    blockEntity.setLevel(world);
                    blockEntity.setBlockState(blockState);
                    BlockEntityRenderState renderState = renderer.createRenderState();
                    renderer.extractRenderState(blockEntity, renderState, 0, cameraPos, null);
                    renderer.submit(renderState, matrices, submitNodeStorage, camera);
                    blockEntity.setBlockState(stateBefore);
                    blockEntity.setLevel(previousLevel);
                }
            }
        }
        matrices.popPose();
        featureRenderDispatcher.renderAllFeatures(submitNodeStorage);
        gameRenderer.useUiLightmap = lightOption;
        texture.clear();
        state.addBlitToCurrentLayer(new BlitRenderState(
            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
            TextureSetup.singleTexture(
                texture.textureView(),
                RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST)
            ),
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
    protected void renderToTexture(
        EntityBlockRenderState state,
        PoseStack matrices,
        SubmitNodeCollector submitNodeCollector
    ) {
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
