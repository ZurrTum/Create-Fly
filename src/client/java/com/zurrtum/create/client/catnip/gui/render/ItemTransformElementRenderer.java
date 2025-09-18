package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.TexturedQuadGuiElementRenderState;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

import java.util.IdentityHashMap;
import java.util.Map;

public class ItemTransformElementRenderer extends SpecialGuiElementRenderer<ItemTransformRenderState> {
    private static final Map<ItemTransformRenderState, GpuTexture> TEXTURES = new IdentityHashMap<>();
    private final MatrixStack matrices = new MatrixStack();
    private int windowScaleFactor;

    public ItemTransformElementRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
        super(vertexConsumers);
    }

    public static void clear(ItemTransformRenderState item) {
        GpuTexture texture = TEXTURES.remove(item);
        if (texture != null) {
            texture.close();
        }
    }

    @Override
    public void render(ItemTransformRenderState item, GuiRenderState state, int windowScaleFactor) {
        if (this.windowScaleFactor != windowScaleFactor) {
            this.windowScaleFactor = windowScaleFactor;
            TEXTURES.values().forEach(GpuTexture::close);
            TEXTURES.clear();
        }
        float size = 0;
        GpuTexture texture = TEXTURES.get(item);
        boolean draw;
        if (texture == null || item.dirty) {
            size = item.size * windowScaleFactor;
            if (item.dirty) {
                item.clearDirty();
                if (texture != null && texture.width() != size) {
                    texture.close();
                    texture = null;
                }
            }
            if (texture == null) {
                texture = GpuTexture.create((int) size);
                TEXTURES.put(item, texture);
            }
            draw = true;
        } else {
            draw = item.state.isAnimated();
        }
        if (draw) {
            if (size == 0) {
                size = item.size * windowScaleFactor;
            }
            RenderSystem.setProjectionMatrix(projectionMatrix.set(size, size), ProjectionType.ORTHOGRAPHIC);
            texture.prepare();
            matrices.push();
            matrices.translate(size / 2, size / 2, 0);
            if (item.padding != 0) {
                size -= item.padding * windowScaleFactor;
            }
            matrices.scale(size, -size, size);
            if (item.zRot != 0) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotation(item.zRot));
            }
            if (item.xRot != 0) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotation(item.xRot));
            }
            if (item.yRot != 0) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotation(item.yRot));
            }
            boolean blockLight = item.state.isSideLit();
            DiffuseLighting lighting = MinecraftClient.getInstance().gameRenderer.getDiffuseLighting();
            if (blockLight) {
                lighting.setShaderLights(DiffuseLighting.Type.ITEMS_3D);
            } else {
                lighting.setShaderLights(DiffuseLighting.Type.ITEMS_FLAT);
            }
            item.state.render(matrices, vertexConsumers, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
            vertexConsumers.draw();
            matrices.pop();
            texture.clear();
        }
        state.addSimpleElementToCurrentLayer(new TexturedQuadGuiElementRenderState(
            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
            TextureSetup.withoutGlTexture(texture.textureView()),
            item.pose,
            item.x1,
            item.y1,
            item.x2,
            item.y2,
            0.0F,
            1.0F,
            1.0F,
            0.0F,
            -1,
            item.scissor,
            null
        ));
    }

    @Override
    protected void render(ItemTransformRenderState item, MatrixStack matrices) {
    }

    @Override
    protected String getName() {
        return "Item Transform";
    }

    @Override
    public Class<ItemTransformRenderState> getElementClass() {
        return ItemTransformRenderState.class;
    }
}
