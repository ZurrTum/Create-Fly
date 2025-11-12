package com.zurrtum.create.client.flywheel.impl;

import com.zurrtum.create.client.flywheel.backend.compile.LightSmoothness;
import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;

public class LightSmoothnessArgument extends StringRepresentableArgument<LightSmoothness> {
    public static final LightSmoothnessArgument INSTANCE = new LightSmoothnessArgument();
    public static final SingletonArgumentInfo<LightSmoothnessArgument> INFO = SingletonArgumentInfo.contextFree(() -> INSTANCE);

    public LightSmoothnessArgument() {
        super(LightSmoothness.CODEC, LightSmoothness::values);
    }
}
