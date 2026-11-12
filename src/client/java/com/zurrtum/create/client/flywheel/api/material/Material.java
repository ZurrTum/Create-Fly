package com.zurrtum.create.client.flywheel.api.material;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public interface Material {
    MaterialShaders shaders();

    FogShader fog();

    CutoutShader cutout();

    LightShader light();

    Identifier texture();

    AbstractTexture gpuTexture();

    default void prepareTexture(TextureManager textureManager) {
    }

    /**
     * Should this material have linear filtering applied to the diffuse sampler?
     *
     * @return {@code true} if this material should be rendered with blur.
     */
    boolean blur();

    boolean mipmap();

    /**
     * Should this material be rendered with backface culling?
     *
     * @return {@code true} if this material should be rendered with backface culling.
     */
    boolean backfaceCulling();

    boolean polygonOffset();

    DepthTest depthTest();

    Transparency transparency();

    WriteMask writeMask();

    boolean useOverlay();

    /**
     * Should this material be rendered with block/sky lighting?
     *
     * @return {@code true} if this material should be rendered with block/sky lighting.
     */
    boolean useLight();

    /**
     * How should this material receive cardinal lighting?
     *
     * @return The cardinal lighting mode.
     */
    CardinalLightingMode cardinalLightingMode();

    /**
     * Whether this material should receive ambient occlusion from nearby chunk geometry.
     *
     * @return {@code true} if this material should receive ambient occlusion.
     */
    default boolean ambientOcclusion() {
        return true;
    }

    /**
     * Check for field-wise equality between this Material and another.
     *
     * @param other The nullable material to check equality against.
     * @return True if the materials represent the same configuration.
     */
    default boolean equals(@Nullable Material other) {
        if (this == other) {
            return true;
        }

        if (other == null) {
            return false;
        }

        // @formatter:off
        return blur() == other.blur()
            && mipmap() == other.mipmap()
            && backfaceCulling() == other.backfaceCulling()
            && polygonOffset() == other.polygonOffset()
            && depthTest() == other.depthTest()
            && transparency() == other.transparency()
            && writeMask() == other.writeMask()
            && useOverlay() == other.useOverlay()
            && useLight() == other.useLight()
            && cardinalLightingMode() == other.cardinalLightingMode()
            && ambientOcclusion() == other.ambientOcclusion()
            && shaders().fragmentSource().equals(other.shaders().fragmentSource())
            && shaders().vertexSource().equals(other.shaders().vertexSource())
            && fog().source().equals(other.fog().source())
            && cutout().source().equals(other.cutout().source())
            && light().source().equals(other.light().source())
            && texture().equals(other.texture());
        // @formatter:on
    }
}
