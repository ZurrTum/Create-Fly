package com.zurrtum.create.client.flywheel.impl;

import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.impl.util.version.StringVersion;
import net.minecraft.command.argument.ArgumentTypes;
import org.jetbrains.annotations.UnknownNullability;

public final class Flywheel {
    public static final String MOD_ID = "flywheel";
    @UnknownNullability
    private static final Version version = new StringVersion("1.0.6+create");

    public void onInitializeClient() {
        setupImpl();
        FlwImpl.init();
    }

    private static void setupImpl() {
        // We can't use ArgumentTypeRegistry from Fabric API here as it also registers to BuiltInRegistries.COMMAND_ARGUMENT_TYPE.
        // We can't register anything to BuiltInRegistries.COMMAND_ARGUMENT_TYPE because it is a synced registry but
        // Flywheel is a client-side only mod.
        ArgumentTypes.CLASS_MAP.put(BackendArgument.class, BackendArgument.INFO);
        ArgumentTypes.CLASS_MAP.put(DebugModeArgument.class, DebugModeArgument.INFO);
        ArgumentTypes.CLASS_MAP.put(LightSmoothnessArgument.class, LightSmoothnessArgument.INFO);
    }

    public static Version version() {
        return version;
    }
}
