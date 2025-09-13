package com.zurrtum.create.client.flywheel.impl;

import com.zurrtum.create.client.flywheel.backend.FlwBackend;
import com.zurrtum.create.client.flywheel.impl.registry.IdRegistryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.zurrtum.create.client.flywheel.impl.Flywheel.MOD_ID;

public final class FlwImpl {
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Logger CONFIG_LOGGER = LoggerFactory.getLogger(MOD_ID + "/config");

    private FlwImpl() {
    }

    public static void init() {
        // impl
        BackendManagerImpl.init();

        // backend
        FabricFlwConfig.INSTANCE.register();
        FlwBackend.init(FlwConfig.INSTANCE.backendConfig());
    }

    public static void freezeRegistries() {
        IdRegistryImpl.freezeAll();
    }
}
