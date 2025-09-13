package com.zurrtum.create.client.foundation.gui;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.catnip.gui.TextureSheetSegment;
import com.zurrtum.create.client.catnip.gui.UIRenderHelper;
import com.zurrtum.create.client.catnip.gui.UIRenderHelper.CustomRenderTarget;
import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.TexturedQuadGuiElementRenderState;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.texture.GlTexture;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public interface ScreenWithStencils {
    List<Pair<Boolean, Runnable>> items = new ArrayList<>();

    default void drawStretched(
        VertexConsumerProvider.Immediate vertexConsumers,
        Matrix3x2f matrixStack,
        int left,
        int top,
        int width,
        int height,
        int z,
        TextureSheetSegment tex
    ) {
        drawTexturedQuad(
            vertexConsumers,
            matrixStack,
            tex.getLocation(),
            left,
            top,
            z,
            tex.getStartX(),
            tex.getStartY(),
            width,
            height,
            tex.getWidth(),
            tex.getHeight(),
            256,
            256
        );
    }

    default void drawTexture(VertexConsumerProvider.Immediate vertexConsumers, Matrix3x2f matrixStack, int x, int y, int z, TextureSheetSegment tex) {
        int width = tex.getWidth();
        int height = tex.getHeight();
        drawTexturedQuad(
            vertexConsumers,
            matrixStack,
            tex.getLocation(),
            x,
            y,
            z,
            tex.getStartX(),
            tex.getStartY(),
            width,
            height,
            width,
            height,
            256,
            256
        );
    }

    default void drawTexture(
        VertexConsumerProvider.Immediate vertexConsumers,
        Matrix3x2f matrixStack,
        Identifier location,
        int x,
        int y,
        int z,
        float u,
        float v,
        int width,
        int height,
        int textureWidth,
        int textureHeight
    ) {
        drawTexturedQuad(vertexConsumers, matrixStack, location, x, y, z, u, v, width, height, width, height, textureWidth, textureHeight);
    }

    default void drawTexturedQuad(
        VertexConsumerProvider.Immediate vertexConsumers,
        Matrix3x2f matrixStack,
        Identifier location,
        float x,
        float y,
        float z,
        float u,
        float v,
        int width,
        int height,
        int regionWidth,
        int regionHeight,
        int textureWidth,
        int textureHeight
    ) {
        float x2 = x + width;
        float y2 = y + height;
        float u1 = u / textureWidth;
        float u2 = (u + regionWidth) / textureWidth;
        float v1 = v / textureHeight;
        float v2 = (v + regionHeight) / textureHeight;
        RenderLayer layer = PonderRenderTypes.getGuiTextured(location);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(layer);
        vertexConsumer.vertex(matrixStack, x, y, z).texture(u1, v1).color(-1);
        vertexConsumer.vertex(matrixStack, x, y2, z).texture(u1, v2).color(-1);
        vertexConsumer.vertex(matrixStack, x2, y2, z).texture(u2, v2).color(-1);
        vertexConsumer.vertex(matrixStack, x2, y, z).texture(u2, v1).color(-1);
    }

    default void fillGradient(
        VertexConsumerProvider.Immediate vertexConsumers,
        Matrix3x2f matrixStack,
        float startX,
        float startY,
        float endX,
        float endY,
        float z,
        int colorStart,
        int colorEnd
    ) {
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(PonderRenderTypes.getGui());
        vertexConsumer.vertex(matrixStack, startX, startY, z).color(colorStart);
        vertexConsumer.vertex(matrixStack, startX, endY, z).color(colorEnd);
        vertexConsumer.vertex(matrixStack, endX, endY, z).color(colorEnd);
        vertexConsumer.vertex(matrixStack, endX, startY, z).color(colorStart);
    }

    default void fill(
        VertexConsumerProvider.Immediate vertexConsumers,
        Matrix3x2f matrixStack,
        float startX,
        float startY,
        float endX,
        float endY,
        float z,
        int color
    ) {
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(PonderRenderTypes.getGui());
        vertexConsumer.vertex(matrixStack, startX, startY, z).color(color);
        vertexConsumer.vertex(matrixStack, startX, endY, z).color(color);
        vertexConsumer.vertex(matrixStack, endX, endY, z).color(color);
        vertexConsumer.vertex(matrixStack, endX, startY, z).color(color);
    }

    default void drawSelection(VertexConsumerProvider.Immediate vertexConsumers, Matrix3x2f matrixStack, int x1, int y1, int x2, int y2, int z) {
        if (x1 < x2) {
            int i = x1;
            x1 = x2;
            x2 = i;
        }
        if (y1 < y2) {
            int i = y1;
            y1 = y2;
            y2 = i;
        }
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(PonderRenderTypes.getGuiInvert());
        vertexConsumer.vertex(matrixStack, x1, y1, z).color(-1);
        vertexConsumer.vertex(matrixStack, x1, y2, z).color(-1);
        vertexConsumer.vertex(matrixStack, x2, y2, z).color(-1);
        vertexConsumer.vertex(matrixStack, x2, y1, z).color(-1);
        vertexConsumer = vertexConsumers.getBuffer(PonderRenderTypes.getGuiTextHighlight());
        int blue = Colors.BLUE;
        vertexConsumer.vertex(matrixStack, x1, y1, z).color(blue);
        vertexConsumer.vertex(matrixStack, x1, y2, z).color(blue);
        vertexConsumer.vertex(matrixStack, x2, y2, z).color(blue);
        vertexConsumer.vertex(matrixStack, x2, y1, z).color(blue);
    }

    default void renderWidget(TextFieldWidget widget, VertexConsumerProvider.Immediate vertexConsumers, Matrix3x2f matrixStack, int z) {
        if (widget.isVisible()) {
            BiFunction<String, Integer, OrderedText> renderTextProvider = widget.renderTextProvider;
            int i = widget.editable ? widget.editableColor : widget.uneditableColor;
            int firstCharacterIndex = widget.firstCharacterIndex;
            TextRenderer textRenderer = widget.textRenderer;
            Text placeholder = widget.placeholder;
            String suggestion = widget.suggestion;
            boolean textShadow = widget.textShadow;
            int textX = widget.textX;
            int textY = widget.textY;
            int cursor = widget.getCursor();
            String text = widget.getText();
            boolean focused = widget.isFocused();
            int width = widget.getWidth();

            String string = textRenderer.trimToWidth(text.substring(firstCharacterIndex), widget.getInnerWidth());
            int j = cursor - firstCharacterIndex;
            boolean bl = j >= 0 && j <= string.length();
            boolean bl2 = focused && (Util.getMeasuringTimeMs() - widget.lastSwitchFocusTime) / 300L % 2L == 0L && bl;
            int k = textX;
            int l = MathHelper.clamp(widget.selectionEnd - firstCharacterIndex, 0, string.length());
            if (!string.isEmpty()) {
                String string2 = bl ? string.substring(0, j) : string;
                OrderedText orderedText = renderTextProvider.apply(string2, firstCharacterIndex);
                drawText(vertexConsumers, matrixStack, textRenderer, orderedText, k, textY, z, i, textShadow);
                k += textRenderer.getWidth(orderedText) + 1;
            }

            boolean bl3 = cursor < text.length() || text.length() >= widget.getMaxLength();
            int m = k;
            if (!bl) {
                m = j > 0 ? textX + width : textX;
            } else if (bl3) {
                m = k - 1;
                k--;
            }

            if (!string.isEmpty() && bl && j < string.length()) {
                drawText(
                    vertexConsumers,
                    matrixStack,
                    textRenderer,
                    renderTextProvider.apply(string.substring(j), cursor),
                    k,
                    textY,
                    z,
                    i,
                    textShadow
                );
            }

            if (placeholder != null && string.isEmpty() && !focused) {
                drawText(vertexConsumers, matrixStack, textRenderer, placeholder, k, textY, z, i, true);
            }

            if (!bl3 && suggestion != null) {
                drawText(vertexConsumers, matrixStack, textRenderer, suggestion, m - 1, textY, z, Colors.GRAY, textShadow);
            }

            if (bl2) {
                if (bl3) {
                    fill(vertexConsumers, matrixStack, m, textY - 1, m + 1, textY + 1 + 9, z, -3092272);
                } else {
                    drawText(vertexConsumers, matrixStack, textRenderer, "_", m, textY, z, i, textShadow);
                }
            }

            if (l != j) {
                int n = textX + textRenderer.getWidth(string.substring(0, l));
                int x = widget.getX();
                drawSelection(vertexConsumers, matrixStack, Math.min(m, x + width), textY - 1, Math.min(n - 1, x + width), textY + 1 + 9, z);
            }
        }
    }

    default void drawItem(
        MinecraftClient client,
        VertexConsumerProvider.Immediate vertexConsumers,
        Matrix3x2f matrixStack,
        ItemStack stack,
        int x,
        int y,
        @Nullable Runnable after
    ) {
        MatrixStack ms = new MatrixStack();
        ms.translate(8 * matrixStack.m00 + matrixStack.m20 + x, 8 * matrixStack.m11 + matrixStack.m21 + y, 0);
        ms.scale(16 * matrixStack.m00, -16 * matrixStack.m11, 16 * matrixStack.m00);
        ItemRenderState state = new ItemRenderState();
        state.displayContext = ItemDisplayContext.GUI;
        client.getItemModelManager().update(state, stack, ItemDisplayContext.GUI, null, null, 0);
        items.add(Pair.of(
            state.isSideLit(), () -> {
                state.render(ms, vertexConsumers, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
                if (after != null) {
                    after.run();
                }
            }
        ));
    }

    default void drawText(
        VertexConsumerProvider.Immediate vertexConsumers,
        Matrix3x2f matrixStack,
        TextRenderer textRenderer,
        String string,
        int x,
        int y,
        int z,
        int color,
        boolean shadow
    ) {
        textRenderer.draw(
            string,
            x,
            y,
            color,
            shadow,
            new Matrix4f().translate(matrixStack.m20, matrixStack.m21, z),
            vertexConsumers,
            TextRenderer.TextLayerType.NORMAL,
            0,
            LightmapTextureManager.MAX_LIGHT_COORDINATE
        );
    }

    default void drawText(
        VertexConsumerProvider.Immediate vertexConsumers,
        Matrix3x2f matrixStack,
        TextRenderer textRenderer,
        Text string,
        int x,
        int y,
        int z,
        int color,
        boolean shadow
    ) {
        textRenderer.draw(
            string,
            x,
            y,
            color,
            shadow,
            new Matrix4f().translate(matrixStack.m20, matrixStack.m21, z),
            vertexConsumers,
            TextRenderer.TextLayerType.NORMAL,
            0,
            LightmapTextureManager.MAX_LIGHT_COORDINATE
        );
    }

    default void drawText(
        VertexConsumerProvider.Immediate vertexConsumers,
        Matrix3x2f matrixStack,
        TextRenderer textRenderer,
        OrderedText string,
        int x,
        int y,
        int z,
        int color,
        boolean shadow
    ) {
        textRenderer.draw(
            string,
            x,
            y,
            color,
            shadow,
            new Matrix4f().translate(matrixStack.m20, matrixStack.m21, z),
            vertexConsumers,
            TextRenderer.TextLayerType.NORMAL,
            0,
            LightmapTextureManager.MAX_LIGHT_COORDINATE
        );
    }

    default void useItemsProjectionMatrix(MinecraftClient client) {
        Window window = client.getWindow();
        RenderSystem.setProjectionMatrix(
            client.gameRenderer.guiRenderer.itemsProjectionMatrix.set(
                (float) window.getFramebufferWidth() / window.getScaleFactor(),
                (float) window.getFramebufferHeight() / window.getScaleFactor()
            ),
            ProjectionType.ORTHOGRAPHIC
        );
        RenderSystem.getModelViewStack().popMatrix();
    }

    default void useGuiProjectionMatrix(MinecraftClient client) {
        RenderSystem.setProjectionMatrix(client.gameRenderer.guiRenderer.guiProjectionMatrix.slice, ProjectionType.ORTHOGRAPHIC);
        RenderSystem.getModelViewStack().pushMatrix().translate(0, 0, -11000);
    }

    default void prepareFrameBuffer(MinecraftClient client, VertexConsumerProvider.Immediate vertexConsumers) {
        vertexConsumers.draw();
        CustomRenderTarget framebuffer = UIRenderHelper.framebuffer;
        int depthAttachmentId = ((GlTexture) framebuffer.getDepthAttachment()).getGlId();
        int frameBufferId = ((GlTexture) framebuffer.getColorAttachment()).depthTexToFramebufferIdCache.get(depthAttachmentId);
        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, frameBufferId);
        GlStateManager._disableScissorTest();
        GL11.glClearDepth(1);
        GL11.glClearColor(0, 0, 0, 0);
        GlStateManager._depthMask(true);
        GlStateManager._colorMask(true, true, true, true);
        GlStateManager._clear(GlConst.GL_DEPTH_BUFFER_BIT | GlConst.GL_COLOR_BUFFER_BIT);
        AbstractSimiContainerScreen.backupProjectionMatrix();
        useGuiProjectionMatrix(client);
        RenderSystem.outputColorTextureOverride = framebuffer.getColorAttachmentView();
        RenderSystem.outputDepthTextureOverride = framebuffer.getDepthAttachmentView();
    }

    default void endFrameBuffer(MinecraftClient client, VertexConsumerProvider.Immediate vertexConsumers, DrawContext graphics) {
        draw(client, vertexConsumers);
        RenderSystem.outputColorTextureOverride = null;
        RenderSystem.outputDepthTextureOverride = null;
        RenderSystem.getModelViewStack().popMatrix();
        AbstractSimiContainerScreen.restoreProjectionMatrix();
        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0);
        Window window = client.getWindow();
        graphics.state.addSimpleElementToCurrentLayer(new TexturedQuadGuiElementRenderState(
            RenderPipelines.GUI_TEXTURED,
            TextureSetup.withoutGlTexture(UIRenderHelper.framebuffer.getColorAttachmentView()),
            new Matrix3x2f(),
            0,
            0,
            window.getScaledWidth(),
            window.getScaledHeight(),
            0.0F,
            1.0F,
            1.0F,
            0.0F,
            -1,
            null,
            null
        ));
    }

    default void draw(MinecraftClient client, VertexConsumerProvider.Immediate vertexConsumers) {
        vertexConsumers.draw();
        if (items.isEmpty()) {
            return;
        }
        useItemsProjectionMatrix(client);
        DiffuseLighting lighting = client.gameRenderer.getDiffuseLighting();
        lighting.setShaderLights(DiffuseLighting.Type.ITEMS_3D);
        boolean blockLight = true;
        for (Pair<Boolean, Runnable> task : items) {
            if (task.getFirst()) {
                if (!blockLight) {
                    lighting.setShaderLights(DiffuseLighting.Type.ITEMS_3D);
                    blockLight = true;
                }
            } else if (blockLight) {
                lighting.setShaderLights(DiffuseLighting.Type.ITEMS_FLAT);
                blockLight = false;
            }
            task.getSecond().run();
            vertexConsumers.draw();
        }
        useGuiProjectionMatrix(client);
        items.clear();
    }

    default void startStencil(
        MinecraftClient client,
        VertexConsumerProvider.Immediate vertexConsumers,
        Matrix3x2f matrixStack,
        float x,
        float y,
        float w,
        float h
    ) {
        draw(client, vertexConsumers);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilFunc(GL11.GL_NEVER, 1, 0xFF);
        fillGradient(vertexConsumers, matrixStack, x, y, x + w, y + h, 0, 0xFF000000, 0xFF000000);
        vertexConsumers.draw(PonderRenderTypes.getGui());
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
    }

    default void endStencil(
        MinecraftClient client,
        VertexConsumerProvider.Immediate vertexConsumers,
        Matrix3x2f matrixStack,
        float x,
        float y,
        float w,
        float h
    ) {
        draw(client, vertexConsumers);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilFunc(GL11.GL_NEVER, 0, 0xFF);
        fillGradient(vertexConsumers, matrixStack, x, y, x + w, y + h, 0, 0xFF000000, 0xFF000000);
        vertexConsumers.draw(PonderRenderTypes.getGui());
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }
}
