package com.zurrtum.create.client.flywheel.backend.engine.uniform;

import net.minecraft.client.option.GameOptions;

public final class OptionsUniforms extends UniformWriter {
    private static final int SIZE = 4 * 14;
    static final UniformBuffer BUFFER = new UniformBuffer(Uniforms.OPTIONS_INDEX, SIZE);

    public static void update(GameOptions options) {
        long ptr = BUFFER.ptr();

        ptr = writeFloat(ptr, options.getGamma().getValue().floatValue());
        ptr = writeInt(ptr, options.getFov().getValue());
        ptr = writeFloat(ptr, options.getDistortionEffectScale().getValue().floatValue());
        ptr = writeFloat(ptr, options.getGlintSpeed().getValue().floatValue());
        ptr = writeFloat(ptr, options.getGlintStrength().getValue().floatValue());
        ptr = writeInt(ptr, options.getBiomeBlendRadius().getValue());
        ptr = writeInt(ptr, options.getAo().getValue() ? 1 : 0);
        ptr = writeInt(ptr, options.getBobView().getValue() ? 1 : 0);
        ptr = writeInt(ptr, options.getHighContrast().getValue() ? 1 : 0);
        ptr = writeFloat(ptr, options.getTextBackgroundOpacity().getValue().floatValue());
        ptr = writeInt(ptr, options.getBackgroundForChatOnly().getValue() ? 1 : 0);
        ptr = writeFloat(ptr, options.getDarknessEffectScale().getValue().floatValue());
        ptr = writeFloat(ptr, options.getDamageTiltStrength().getValue().floatValue());
        ptr = writeInt(ptr, options.getHideLightningFlashes().getValue() ? 1 : 0);

        BUFFER.markDirty();
    }
}
