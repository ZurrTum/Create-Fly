package com.zurrtum.create.client.flywheel.backend.engine;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zurrtum.create.client.flywheel.backend.Samplers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.GlTexture;
import net.minecraft.util.Identifier;

public class TextureBinder {
    public static void bind(Identifier resourceLocation) {
        GlStateManager._bindTexture(byName(resourceLocation));
    }

    public static void bindLightAndOverlay() {
        var gameRenderer = MinecraftClient.getInstance().gameRenderer;

        Samplers.OVERLAY.makeActive();
        gameRenderer.getOverlayTexture().setupOverlayColor();
        GlStateManager._bindTexture(((GlTexture) RenderSystem.getShaderTexture(1).texture()).getGlId());

        Samplers.LIGHT.makeActive();
        gameRenderer.getLightmapTextureManager().enable();
        GlStateManager._bindTexture(((GlTexture) RenderSystem.getShaderTexture(2).texture()).getGlId());
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
