package com.zurrtum.create.client.flywheel.impl;

import com.zurrtum.create.client.flywheel.backend.compile.LightSmoothness;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;

public class LightSmoothnessArgument extends EnumArgumentType<LightSmoothness> {
    public static final LightSmoothnessArgument INSTANCE = new LightSmoothnessArgument();
    public static final ConstantArgumentSerializer<LightSmoothnessArgument> INFO = ConstantArgumentSerializer.of(() -> INSTANCE);

    public LightSmoothnessArgument() {
        super(LightSmoothness.CODEC, LightSmoothness::values);
    }
}
