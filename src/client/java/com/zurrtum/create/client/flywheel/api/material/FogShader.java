package com.zurrtum.create.client.flywheel.api.material;

import net.minecraft.util.Identifier;

/**
 * A shader that controls the fog effect on a material.
 */
public interface FogShader {
    /**
     * @apiNote {@code flywheel/} is implicitly prepended to the {@link Identifier}'s path.
     */
    Identifier source();
}
