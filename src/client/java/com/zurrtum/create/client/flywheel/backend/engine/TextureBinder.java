package com.zurrtum.create.client.flywheel.backend.engine;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.zurrtum.create.client.flywheel.backend.Samplers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL12;

public class TextureBinder {
    public static void bind(ResourceLocation resourceLocation) {
        GlStateManager._bindTexture(byName(resourceLocation));
    }

    public static void bindCrumbling(ResourceLocation resourceLocation) {
        Samplers.CRUMBLING.makeActive();
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(resourceLocation);
        setupTexture(texture.getTextureView());
    }

    public static void bindLightAndOverlay() {
        var gameRenderer = Minecraft.getInstance().gameRenderer;

        Samplers.OVERLAY.makeActive();
        gameRenderer.overlayTexture().setupOverlayColor();
        setupTexture(RenderSystem.getShaderTexture(1));

        Samplers.LIGHT.makeActive();
        gameRenderer.lightTexture().turnOnLightLayer();
        setupTexture(RenderSystem.getShaderTexture(2));
    }

    private static void setupTexture(@Nullable GpuTextureView textureView) {
        if (textureView == null) {
            return;
        }
        GlTexture texture = (GlTexture) textureView.texture();
        GlStateManager._bindTexture(texture.glId());
        GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, textureView.baseMipLevel());
        GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, textureView.baseMipLevel() + textureView.mipLevels() - 1);
        texture.flushModeChanges(GlConst.GL_TEXTURE_2D);
    }

    public static void resetLightAndOverlay() {
        var gameRenderer = Minecraft.getInstance().gameRenderer;

        gameRenderer.overlayTexture().teardownOverlayColor();
        gameRenderer.lightTexture().turnOffLightLayer();
    }

    /**
     * Get a built-in texture by its resource location.
     *
     * @param texture The texture's resource location.
     * @return The texture.
     */
    public static int byName(ResourceLocation texture) {
        return ((GlTexture) Minecraft.getInstance().getTextureManager().getTexture(texture).getTexture()).glId();
    }
}
