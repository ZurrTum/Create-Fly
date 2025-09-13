package com.zurrtum.create.client.flywheel.impl;

import com.zurrtum.create.client.flywheel.backend.engine.uniform.DebugMode;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;

public class DebugModeArgument extends EnumArgumentType<DebugMode> {
    public static final DebugModeArgument INSTANCE = new DebugModeArgument();
    public static final ConstantArgumentSerializer<DebugModeArgument> INFO = ConstantArgumentSerializer.of(() -> INSTANCE);

    public DebugModeArgument() {
        super(DebugMode.CODEC, DebugMode::values);
    }
}
