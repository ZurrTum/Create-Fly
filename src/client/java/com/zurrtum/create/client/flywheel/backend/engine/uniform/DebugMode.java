package com.zurrtum.create.client.flywheel.backend.engine.uniform;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

import java.util.Locale;

public enum DebugMode implements StringIdentifiable {
    OFF,
    NORMALS,
    INSTANCE_ID,
    LIGHT_LEVEL,
    LIGHT_COLOR,
    OVERLAY,
    DIFFUSE,
    MODEL_ID;

    public static final Codec<DebugMode> CODEC = StringIdentifiable.createCodec(DebugMode::values);

    @Override
    public String asString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
