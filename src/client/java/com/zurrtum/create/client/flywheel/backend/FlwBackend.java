package com.zurrtum.create.client.flywheel.backend;

import org.jetbrains.annotations.UnknownNullability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FlwBackend {
    public static final Logger LOGGER = LoggerFactory.getLogger("flywheel/backend");
    private static @UnknownNullability BackendConfig config;

    private FlwBackend() {
    }

    public static BackendConfig config() {
        return config;
    }

    public static void init(BackendConfig config) {
        FlwBackend.config = config;
        Backends.init();
    }
}
