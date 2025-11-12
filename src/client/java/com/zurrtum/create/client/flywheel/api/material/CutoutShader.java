package com.zurrtum.create.client.flywheel.api.material;

import net.minecraft.resources.ResourceLocation;

/**
 * A shader that decides what colors should be discarded in the fragment shader.
 */
public interface CutoutShader {
    /**
     * @apiNote {@code flywheel/} is implicitly prepended to the {@link ResourceLocation}'s path.
     */
    ResourceLocation source();
}
