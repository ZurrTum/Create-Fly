package com.zurrtum.create.client.flywheel.api.material;

public enum CardinalLightingMode {
    /**
     * No normal-based darkening will be applied.
     */
    OFF,

    /**
     * World-space normal based darkening will be applied.
     *
     * <p>This mode matches the appearance of chunk geometry.
     */
    CHUNK,

    /**
     * World-space normal based darkening will be applied in accordance to the "light directions" specified in RenderSystem.
     *
     * <p>This mode matches the appearance of entities.
     *
     * @see com.mojang.blaze3d.systems.RenderSystem#setShaderLights
     */
    ENTITY;
}
