package com.zurrtum.create.client.flywheel.backend.engine;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.opengl.GlConst;
import com.mojang.renderpearl.backend.opengl.GlSampler;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.backend.opengl.GlTexture;
import com.zurrtum.create.client.flywheel.backend.Samplers;
import com.zurrtum.create.client.flywheel.backend.gl.GlTextureUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL33C;

public class TextureBinder {
    public static void bindLightAndOverlay() {
        GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        GpuSampler sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        setupTexture(Samplers.OVERLAY, gameRenderer.overlayTexture().getTextureView(), sampler);
        setupTexture(Samplers.LIGHT, gameRenderer.lightmap(), sampler);
    }

    public static void setupTexture(GlTextureUnit unit, GpuTextureView textureView, GpuSampler sampler) {
        unit.makeActive();
        GlTexture texture = (GlTexture) textureView.texture();
        int target;
        if ((texture.usage() & 16) != 0) {
            target = GL13.GL_TEXTURE_CUBE_MAP;
            GL11.glBindTexture(target, texture.glId());
        } else {
            target = GlConst.GL_TEXTURE_2D;
            GlStateManager._bindTexture(texture.glId());
        }
        GL33C.glBindSampler(unit.number, ((GlSampler) sampler).getId());
        int mipLevel = textureView.baseMipLevel();
        GlStateManager._texParameter(target, GL12.GL_TEXTURE_BASE_LEVEL, mipLevel);
        GlStateManager._texParameter(target, GL12.GL_TEXTURE_MAX_LEVEL, mipLevel + textureView.mipLevels() - 1);
    }
}
