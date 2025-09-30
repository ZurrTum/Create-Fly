package com.zurrtum.create.client.flywheel.backend.engine;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.zurrtum.create.client.flywheel.backend.Samplers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.GlTexture;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL12;

public class TextureBinder {
    public static void bind(Identifier resourceLocation) {
        GlStateManager._bindTexture(byName(resourceLocation));
    }

    public static void bindCrumbling(Identifier resourceLocation) {
        Samplers.CRUMBLING.makeActive();
        AbstractTexture texture = MinecraftClient.getInstance().getTextureManager().getTexture(resourceLocation);
        setupTexture(texture.getGlTextureView());
    }

    public static void bindLightAndOverlay() {
        var gameRenderer = MinecraftClient.getInstance().gameRenderer;

        Samplers.OVERLAY.makeActive();
        gameRenderer.getOverlayTexture().setupOverlayColor();
        setupTexture(RenderSystem.getShaderTexture(1));

        Samplers.LIGHT.makeActive();
        gameRenderer.getLightmapTextureManager().enable();
        setupTexture(RenderSystem.getShaderTexture(2));
    }

    private static void setupTexture(@Nullable GpuTextureView textureView) {
        if (textureView == null) {
            return;
        }
        GlTexture texture = (GlTexture) textureView.texture();
        GlStateManager._bindTexture(texture.getGlId());
        GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, textureView.baseMipLevel());
        GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, textureView.baseMipLevel() + textureView.mipLevels() - 1);
        texture.checkDirty(GlConst.GL_TEXTURE_2D);
    }

    public static void resetLightAndOverlay() {
        var gameRenderer = MinecraftClient.getInstance().gameRenderer;

        gameRenderer.getOverlayTexture().teardownOverlayColor();
        gameRenderer.getLightmapTextureManager().disable();
    }

    /**
     * Get a built-in texture by its resource location.
     *
     * @param texture The texture's resource location.
     * @return The texture.
     */
    public static int byName(Identifier texture) {
        return ((GlTexture) MinecraftClient.getInstance().getTextureManager().getTexture(texture).getGlTexture()).getGlId();
    }
}
