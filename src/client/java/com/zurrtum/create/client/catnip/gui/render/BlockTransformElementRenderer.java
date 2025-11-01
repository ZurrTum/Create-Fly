package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.TexturedQuadGuiElementRenderState;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;

import java.util.HashMap;
import java.util.Map;

public class BlockTransformElementRenderer extends SpecialGuiElementRenderer<BlockTransformRenderState> {
    private static final Map<Object, GpuTexture> TEXTURES = new HashMap<>();
    private final MatrixStack matrices = new MatrixStack();
    private int windowScaleFactor;

    public BlockTransformElementRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
        super(vertexConsumers);
    }

    public static void clear(Object key) {
        GpuTexture texture = TEXTURES.remove(key);
        if (texture != null) {
            texture.close();
        }
    }

    @Override
    public void render(BlockTransformRenderState block, GuiRenderState state, int windowScaleFactor) {
        if (this.windowScaleFactor != windowScaleFactor) {
            this.windowScaleFactor = windowScaleFactor;
            TEXTURES.values().forEach(GpuTexture::close);
            TEXTURES.clear();
        }
        Object key = block.getKey();
        GpuTexture texture = TEXTURES.get(key);
        if (texture == null) {
            float size = block.size() * windowScaleFactor;
            texture = GpuTexture.create((int) size);
            TEXTURES.put(key, texture);
            RenderSystem.setProjectionMatrix(projectionMatrix.set(size, size), ProjectionType.ORTHOGRAPHIC);
            texture.prepare();
            matrices.push();
            matrices.translate(size / 2, size / 2, 0);
            if (block.padding() != 0) {
                size -= block.padding() * windowScaleFactor;
            }
            matrices.scale(size, size, size);
            if (block.zRot() != 0) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotation(block.zRot()));
            }
            if (block.xRot() != 0) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotation(block.xRot()));
            }
            if (block.yRot() != 0) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotation(block.yRot()));
            }
            matrices.scale(1, -1, 1);
            matrices.translate(-0.5F, -0.5F, -0.5F);
            MinecraftClient mc = MinecraftClient.getInstance();
            RenderLayer layer;
            if (block.state().isOf(Blocks.REDSTONE_TORCH) && block.state().get(RedstoneTorchBlock.LIT)) {
                layer = RenderLayer.getCutout();
            } else {
                layer = RenderLayers.getBlockLayer(block.state()) == BlockRenderLayer.TRANSLUCENT ? TexturedRenderLayers.getItemEntityTranslucentCull() : TexturedRenderLayers.getEntityCutout();
            }
            SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
            world.blockState(block.state());
            mc.getBlockRenderManager()
                .renderBlock(block.state(), BlockPos.ORIGIN, world, matrices, vertexConsumers.getBuffer(layer), false, block.parts());
            vertexConsumers.draw();
            matrices.pop();
            texture.clear();
        }
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
            block.scissor(),
            null
        ));
    }

    @Override
    protected void render(BlockTransformRenderState block, MatrixStack matrices) {
    }

    @Override
    protected String getName() {
        return "Block Transform";
    }

    @Override
    public Class<BlockTransformRenderState> getElementClass() {
        return BlockTransformRenderState.class;
    }
}
