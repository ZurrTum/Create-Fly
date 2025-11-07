package com.zurrtum.create.client.flywheel.backend.compile;

import com.mojang.serialization.Codec;
import com.zurrtum.create.client.flywheel.backend.compile.core.Compilation;
import net.minecraft.util.StringIdentifiable;

import java.util.Locale;

public enum LightSmoothness implements StringIdentifiable {
    FLAT(0, false),
    TRI_LINEAR(1, false),
    SMOOTH(2, false),
    SMOOTH_INNER_FACE_CORRECTED(2, true);

    public static final Codec<LightSmoothness> CODEC = StringIdentifiable.createCodec(LightSmoothness::values);

    private final int smoothnessDefine;
    private final boolean innerFaceCorrection;

    LightSmoothness(int smoothnessDefine, boolean innerFaceCorrection) {
        this.smoothnessDefine = smoothnessDefine;
        this.innerFaceCorrection = innerFaceCorrection;
    }

    public void onCompile(Compilation comp) {
        comp.define("_FLW_LIGHT_SMOOTHNESS", Integer.toString(smoothnessDefine));
        if (innerFaceCorrection) {
            comp.define("_FLW_INNER_FACE_CORRECTION");
        }
    }

    @Override
    public String asString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
