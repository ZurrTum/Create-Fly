package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.TexturedQuadGuiElementRenderState;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.List;

public class EntityBlockRenderer extends SpecialGuiElementRenderer<EntityBlockRenderState> {
    private static final Int2ObjectMap<GpuTexture> TEXTURES = new Int2ObjectArrayMap<>();
    private final MatrixStack matrices = new MatrixStack();
    private int windowScaleFactor;

    public EntityBlockRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
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
    public void render(EntityBlockRenderState block, GuiRenderState state, int windowScaleFactor) {
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
        RenderSystem.setProjectionMatrix(projectionMatrix.set(size, size), ProjectionType.ORTHOGRAPHIC);
        texture.prepare();
        matrices.push();
        matrices.translate(size / 2, size / 2, 0);
        float scale = block.scale() * windowScaleFactor;
        matrices.scale(scale, -scale, scale);
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ENTITY_IN_UI);
        if (block.zRot() != 0) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotation(block.zRot()));
        }
        if (block.xRot() != 0) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotation(block.xRot()));
        }
        if (block.yRot() != 0) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(block.yRot()));
        }
        matrices.translate(-0.5F, -0.5F, -0.5F);
        BlockRenderManager blockRenderManager = mc.getBlockRenderManager();
        World world = block.world();
        BlockState blockState = block.state();
        BlockEntity blockEntity = block.entity();
        RenderLayer layer = RenderLayers.getBlockLayer(blockState) == BlockRenderLayer.TRANSLUCENT ? TexturedRenderLayers.getItemEntityTranslucentCull() : TexturedRenderLayers.getEntityCutout();
        SinglePosVirtualBlockGetter lightWorld = SinglePosVirtualBlockGetter.createFullBright();
        lightWorld.blockState(blockState);
        lightWorld.blockEntity(blockEntity);
        BlockStateModel model = blockRenderManager.getModel(blockState);
        List<BlockModelPart> parts = new ObjectArrayList<>();
        Random random = world.getRandom();
        if (WrapperBlockStateModel.unwrapCompat(model) instanceof WrapperBlockStateModel wrapper) {
            wrapper.addPartsWithInfo(world, block.pos(), blockState, random, parts);
        } else {
            model.addParts(random, parts);
        }
        blockRenderManager.renderBlock(blockState, BlockPos.ORIGIN, lightWorld, matrices, vertexConsumers.getBuffer(layer), false, parts);
        if (blockEntity != null) {
            BlockEntityRenderer<BlockEntity> renderer = mc.getBlockEntityRenderDispatcher().get(blockEntity);
            if (renderer != null) {
                World previousLevel = blockEntity.getWorld();
                BlockState stateBefore = blockEntity.getCachedState();
                blockEntity.setWorld(world);
                blockEntity.setCachedState(blockState);
                renderer.render(
                    blockEntity,
                    0,
                    matrices,
                    vertexConsumers,
                    LightmapTextureManager.MAX_LIGHT_COORDINATE,
                    OverlayTexture.DEFAULT_UV,
                    Vec3d.ZERO
                );
                blockEntity.setCachedState(stateBefore);
                blockEntity.setWorld(previousLevel);
            }
        }
        vertexConsumers.draw();
        matrices.pop();
        texture.clear();
        state.addSimpleElementToCurrentLayer(new TexturedQuadGuiElementRenderState(
            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
            TextureSetup.withoutGlTexture(texture.textureView()),
            block.pose(),
            block.x1(),
            block.y1(),
            block.x2(),
            block.y2(),
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
    protected void render(EntityBlockRenderState state, MatrixStack matrices) {
    }

    @Override
    protected String getName() {
        return "Entity Block";
    }

    @Override
    public Class<EntityBlockRenderState> getElementClass() {
        return EntityBlockRenderState.class;
    }
}
