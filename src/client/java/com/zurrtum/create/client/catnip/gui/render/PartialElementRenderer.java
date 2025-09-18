package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import com.zurrtum.create.client.model.LayerBakedModel;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.TexturedQuadGuiElementRenderState;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class PartialElementRenderer extends SpecialGuiElementRenderer<PartialRenderState> {
    private static final Map<PartialRenderState, GpuTexture> TEXTURES = new IdentityHashMap<>();
    private final MatrixStack matrices = new MatrixStack();
    private int windowScaleFactor;

    public PartialElementRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
        super(vertexConsumers);
    }

    public static void clear(PartialRenderState block) {
        GpuTexture texture = TEXTURES.remove(block);
        if (texture != null) {
            texture.close();
        }
    }

    @Override
    public void render(PartialRenderState partial, GuiRenderState state, int windowScaleFactor) {
        if (this.windowScaleFactor != windowScaleFactor) {
            this.windowScaleFactor = windowScaleFactor;
            TEXTURES.values().forEach(GpuTexture::close);
            TEXTURES.clear();
        }
        GpuTexture texture = TEXTURES.get(partial);
        boolean draw = texture == null || partial.dirty;
        if (draw) {
            float size = partial.size * windowScaleFactor;
            if (partial.dirty) {
                partial.clearDirty();
                if (texture != null && texture.width() != size) {
                    texture.close();
                    texture = null;
                }
            }
            if (texture == null) {
                texture = GpuTexture.create((int) size);
                TEXTURES.put(partial, texture);
            }
            RenderSystem.setProjectionMatrix(projectionMatrix.set(size, size), ProjectionType.ORTHOGRAPHIC);
            texture.prepare();
            matrices.push();
            if (partial.padding != 0) {
                size -= partial.padding * windowScaleFactor;
            }
            matrices.scale(size, size, size);
            partial.transform(matrices);
            MinecraftClient mc = MinecraftClient.getInstance();
            BlockRenderLayer blockRenderLayer = LayerBakedModel.getBlockRenderLayer(partial.model, () -> BlockRenderLayer.SOLID);
            RenderLayer layer = blockRenderLayer == BlockRenderLayer.TRANSLUCENT ? TexturedRenderLayers.getItemEntityTranslucentCull() : TexturedRenderLayers.getEntityCutout();
            SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
            mc.getBlockRenderManager().renderBlock(
                Blocks.AIR.getDefaultState(),
                BlockPos.ORIGIN,
                world,
                matrices,
                vertexConsumers.getBuffer(layer),
                false,
                List.of(partial.model)
            );
            vertexConsumers.draw();
            matrices.pop();
            texture.clear();
        }
        state.addSimpleElementToCurrentLayer(new TexturedQuadGuiElementRenderState(
            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
            TextureSetup.withoutGlTexture(texture.textureView()),
            partial.pose,
            partial.x1,
            partial.y1,
            partial.x2,
            partial.y2,
            0.0F,
            1.0F,
            1.0F,
            0.0F,
            -1,
            partial.scissor,
            null
        ));
    }

    @Override
    protected void render(PartialRenderState partial, MatrixStack matrices) {
    }

    @Override
    protected String getName() {
        return "Partial";
    }

    @Override
    public Class<PartialRenderState> getElementClass() {
        return PartialRenderState.class;
    }
}
