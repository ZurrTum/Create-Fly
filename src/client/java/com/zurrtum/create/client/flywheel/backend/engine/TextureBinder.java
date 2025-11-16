package com.zurrtum.create.client.flywheel.backend.engine;

import com.mojang.blaze3d.opengl.GlSampler;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.zurrtum.create.client.flywheel.backend.Samplers;
import com.zurrtum.create.client.flywheel.backend.gl.GlTextureUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL33C;

import static com.mojang.blaze3d.opengl.GlConst.GL_TEXTURE_2D;

public class TextureBinder {
    public static void bind(Identifier Identifier) {
        GlStateManager._bindTexture(byName(Identifier));
    }

    public static void bindCrumbling(Identifier Identifier) {
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(Identifier);
        setupTexture(Samplers.CRUMBLING, texture.getTextureView(), texture.getSampler());
    }

    public static void bindLightAndOverlay() {
        GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        GpuSampler sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        setupTexture(Samplers.OVERLAY, gameRenderer.overlayTexture().getTextureView(), sampler);
        setupTexture(Samplers.LIGHT, gameRenderer.lightTexture().getTextureView(), sampler);
    }

    private static void setupTexture(GlTextureUnit unit, GpuTextureView textureView, GpuSampler sampler) {
        unit.makeActive();
        GlTexture texture = (GlTexture) textureView.texture();
        int target;
        if ((texture.usage() & 16) != 0) {
            target = GL13.GL_TEXTURE_CUBE_MAP;
            GL11.glBindTexture(target, texture.glId());
        } else {
            target = GL_TEXTURE_2D;
            GlStateManager._bindTexture(texture.glId());
        }
        GL33C.glBindSampler(unit.number, ((GlSampler) sampler).getId());
        int mipLevel = textureView.baseMipLevel();
        GlStateManager._texParameter(target, GL12.GL_TEXTURE_BASE_LEVEL, mipLevel);
        GlStateManager._texParameter(target, GL12.GL_TEXTURE_MAX_LEVEL, mipLevel + textureView.mipLevels() - 1);
    }

    public static void resetLightAndOverlay() {
    }

    /**
     * Get a built-in texture by its resource location.
     *
     * @param texture The texture's resource location.
     * @return The texture.
     */
    public static int byName(Identifier texture) {
        return ((GlTexture) Minecraft.getInstance().getTextureManager().getTexture(texture).getTexture()).glId();
    }
}
