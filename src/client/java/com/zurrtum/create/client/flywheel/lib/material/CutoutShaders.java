package com.zurrtum.create.client.flywheel.lib.material;

import com.zurrtum.create.client.flywheel.api.material.CutoutShader;
import com.zurrtum.create.client.flywheel.lib.util.ResourceUtil;

public final class CutoutShaders {
    /**
     * Do not discard any fragments based on alpha.
     */
    public static final CutoutShader OFF = new SimpleCutoutShader(ResourceUtil.rl("cutout/off.glsl"));
    /**
     * Discard fragments with alpha close to or equal to zero.
     */
    public static final CutoutShader EPSILON = new SimpleCutoutShader(ResourceUtil.rl("cutout/epsilon.glsl"));
    /**
     * Discard fragments with alpha less than to 0.1.
     */
    public static final CutoutShader ONE_TENTH = new SimpleCutoutShader(ResourceUtil.rl("cutout/one_tenth.glsl"));
    /**
     * Discard fragments with alpha less than to 0.5.
     */
    public static final CutoutShader HALF = new SimpleCutoutShader(ResourceUtil.rl("cutout/half.glsl"));

    private CutoutShaders() {
    }
}
