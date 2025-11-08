package com.zurrtum.create.client.flywheel.backend.engine;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zurrtum.create.client.flywheel.api.material.DepthTest;
import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.api.material.Transparency;
import com.zurrtum.create.client.flywheel.api.material.WriteMask;
import com.zurrtum.create.client.flywheel.backend.Samplers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlBackend;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.GlTexture;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.Comparator;

import static com.mojang.blaze3d.opengl.GlConst.*;

public final class MaterialRenderState {
    public static final Comparator<Material> COMPARATOR = MaterialRenderState::compare;

    private MaterialRenderState() {
    }

    public static void setup(Material material) {
        setupTexture(material);
        setupBackfaceCulling(material.backfaceCulling());
        setupPolygonOffset(material.polygonOffset());
        setupDepthTest(material.depthTest());
        setupTransparency(material.transparency());
        setupWriteMask(material.writeMask());
    }

    public static void setupOit(Material material) {
        setupTexture(material);
        setupBackfaceCulling(material.backfaceCulling());
        setupPolygonOffset(material.polygonOffset());
        setupDepthTest(material.depthTest());

        WriteMask mask = material.writeMask();
        boolean writeColor = mask.color();
        GlStateManager._colorMask(writeColor, writeColor, writeColor, writeColor);
    }

    private static void setupTexture(Material material) {
        Samplers.DIFFUSE.makeActive();
        AbstractTexture texture = MinecraftClient.getInstance().getTextureManager().getTexture(material.texture());
        texture.setFilter(material.blur(), material.mipmap());
        GlTexture glTexture = (GlTexture) texture.getGlTexture();
        var textureId = glTexture.getGlId();
        RenderSystem.setShaderTexture(0, texture.getGlTextureView());
        GlStateManager._bindTexture(textureId);
        glTexture.checkDirty(GlConst.GL_TEXTURE_2D);
    }

    private static void setupBackfaceCulling(boolean backfaceCulling) {
        if (backfaceCulling) {
            GlStateManager._enableCull();
        } else {
            GlStateManager._disableCull();
        }
    }

    private static void setupPolygonOffset(boolean polygonOffset) {
        if (polygonOffset) {
            GlStateManager._polygonOffset(-1.0F, -10.0F);
            GlStateManager._enablePolygonOffset();
        } else {
            GlStateManager._polygonOffset(0.0F, 0.0F);
            GlStateManager._disablePolygonOffset();
        }
    }

    private static void setupDepthTest(DepthTest depthTest) {
        switch (depthTest) {
            case OFF -> {
                GlStateManager._disableDepthTest();
            }
            case NEVER -> {
                GlStateManager._enableDepthTest();
                GlStateManager._depthFunc(GL11.GL_NEVER);
            }
            case LESS -> {
                GlStateManager._enableDepthTest();
                GlStateManager._depthFunc(GL11.GL_LESS);
            }
            case EQUAL -> {
                GlStateManager._enableDepthTest();
                GlStateManager._depthFunc(GL11.GL_EQUAL);
            }
            case LEQUAL -> {
                GlStateManager._enableDepthTest();
                GlStateManager._depthFunc(GL11.GL_LEQUAL);
            }
            case GREATER -> {
                GlStateManager._enableDepthTest();
                GlStateManager._depthFunc(GL11.GL_GREATER);
            }
            case NOTEQUAL -> {
                GlStateManager._enableDepthTest();
                GlStateManager._depthFunc(GL11.GL_NOTEQUAL);
            }
            case GEQUAL -> {
                GlStateManager._enableDepthTest();
                GlStateManager._depthFunc(GL11.GL_GEQUAL);
            }
            case ALWAYS -> {
                GlStateManager._enableDepthTest();
                GlStateManager._depthFunc(GL11.GL_ALWAYS);
            }
        }
    }

    private static void setupTransparency(Transparency transparency) {
        switch (transparency) {
            case OPAQUE -> {
                GlStateManager._disableBlend();
            }
            case ADDITIVE -> {
                GlStateManager._enableBlend();
                GlStateManager._blendFuncSeparate(GL_ONE, GL_ONE, GL_ONE, GL_ONE);
            }
            case LIGHTNING -> {
                GlStateManager._enableBlend();
                GlStateManager._blendFuncSeparate(GL_SRC_ALPHA, GL_ONE, GL_SRC_ALPHA, GL_ONE);
            }
            case GLINT -> {
                GlStateManager._enableBlend();
                GlStateManager._blendFuncSeparate(GL_SRC_COLOR, GL_ONE, GL_ZERO, GL_ONE);
            }
            case CRUMBLING -> {
                GlStateManager._enableBlend();
                GlStateManager._blendFuncSeparate(GL_DST_COLOR, GL_SRC_COLOR, GL_ONE, GL_ZERO);
            }
            case TRANSLUCENT -> {
                GlStateManager._enableBlend();
                GlStateManager._blendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
            }
        }
    }

    private static void setupWriteMask(WriteMask mask) {
        GlStateManager._depthMask(mask.depth());
        boolean writeColor = mask.color();
        GlStateManager._colorMask(writeColor, writeColor, writeColor, writeColor);
    }

    public static void reset() {
        resetFrameBuffer();
        resetTexture();
        resetBackfaceCulling();
        resetPolygonOffset();
        resetDepthTest();
        resetTransparency();
        resetWriteMask();
    }

    public static void setupFrameBuffer() {
        Framebuffer framebuffer = MinecraftClient.getInstance().getFramebuffer();
        int i = ((GlTexture) framebuffer.getColorAttachment()).getOrCreateFramebuffer(
            ((GlBackend) RenderSystem.getDevice()).getBufferManager(),
            framebuffer.useDepthAttachment ? framebuffer.getDepthAttachment() : null
        );
        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, i);
    }

    private static void resetFrameBuffer() {
        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0);
    }

    private static void resetTexture() {
        Samplers.DIFFUSE.makeActive();
        RenderSystem.setShaderTexture(0, null);
    }

    private static void resetBackfaceCulling() {
        GlStateManager._enableCull();
    }

    private static void resetPolygonOffset() {
        GlStateManager._polygonOffset(0.0F, 0.0F);
        GlStateManager._disablePolygonOffset();
    }

    private static void resetDepthTest() {
        GlStateManager._disableDepthTest();
        GlStateManager._depthFunc(GL11.GL_LEQUAL);
    }

    private static void resetTransparency() {
        GlStateManager._disableBlend();
        GlStateManager._blendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
    }

    private static void resetWriteMask() {
        GlStateManager._depthMask(true);
        GlStateManager._colorMask(true, true, true, true);
    }

    public static boolean materialEquals(Material lhs, Material rhs) {
        if (lhs == rhs) {
            return true;
        }

        // Not here because ubershader: useLight, useOverlay, diffuse, fog shader
        // Everything in the comparator should be here.
        // @formatter:off
        return lhs.blur() == rhs.blur()
            && lhs.mipmap() == rhs.mipmap()
            && lhs.backfaceCulling() == rhs.backfaceCulling()
            && lhs.polygonOffset() == rhs.polygonOffset()
            && lhs.depthTest() == rhs.depthTest()
            && lhs.transparency() == rhs.transparency()
            && lhs.writeMask() == rhs.writeMask()
            && lhs.light().source().equals(rhs.light().source())
            && lhs.texture().equals(rhs.texture())
            && lhs.cutout().source().equals(rhs.cutout().source())
            && lhs.shaders().fragmentSource().equals(rhs.shaders().fragmentSource())
            && lhs.shaders().vertexSource().equals(rhs.shaders().vertexSource());
        // @formatter:on
    }

    public static boolean materialIsAllNonNull(@Nullable Material material) {
        // We do not trust people to give us valid NotNull objects.
        // @formatter:off
        return material != null &&
            material.shaders() != null &&
            material.shaders().fragmentSource() != null &&
            material.shaders().vertexSource() != null &&
            material.fog() != null &&
            material.fog().source() != null &&
            material.cutout() != null &&
            material.cutout().source() != null &&
            material.light() != null &&
            material.light().source() != null &&
            material.texture() != null &&
            material.depthTest() != null &&
            material.transparency() != null &&
            material.writeMask() != null &&
            material.cardinalLightingMode() != null;
        // @formatter:on
    }

    public static int compare(Material lhs, Material rhs) {
        if (lhs == rhs) {
            return 0;
        }

        int cmp;
        cmp = lhs.transparency().compareTo(rhs.transparency());
        if (cmp != 0) {
            return cmp;
        }
        cmp = lhs.light().source().compareTo(rhs.light().source());
        if (cmp != 0) {
            return cmp;
        }
        cmp = lhs.cutout().source().compareTo(rhs.cutout().source());
        if (cmp != 0) {
            return cmp;
        }
        cmp = lhs.shaders().fragmentSource().compareTo(rhs.shaders().fragmentSource());
        if (cmp != 0) {
            return cmp;
        }
        cmp = lhs.shaders().vertexSource().compareTo(rhs.shaders().vertexSource());
        if (cmp != 0) {
            return cmp;
        }
        cmp = lhs.texture().compareTo(rhs.texture());
        if (cmp != 0) {
            return cmp;
        }
        cmp = Boolean.compare(lhs.blur(), rhs.blur());
        if (cmp != 0) {
            return cmp;
        }
        cmp = Boolean.compare(lhs.mipmap(), rhs.mipmap());
        if (cmp != 0) {
            return cmp;
        }
        cmp = Boolean.compare(lhs.backfaceCulling(), rhs.backfaceCulling());
        if (cmp != 0) {
            return cmp;
        }
        cmp = Boolean.compare(lhs.polygonOffset(), rhs.polygonOffset());
        if (cmp != 0) {
            return cmp;
        }
        cmp = lhs.depthTest().compareTo(rhs.depthTest());
        if (cmp != 0) {
            return cmp;
        }
        cmp = lhs.writeMask().compareTo(rhs.writeMask());
        if (cmp != 0) {
            return cmp;
        }
        return 0;
    }
}
