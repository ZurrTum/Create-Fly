package com.zurrtum.create.client.flywheel.impl;

import com.zurrtum.create.catnip.config.Builder;
import com.zurrtum.create.client.flywheel.api.backend.Backend;
import com.zurrtum.create.client.flywheel.api.backend.BackendManager;
import com.zurrtum.create.client.flywheel.backend.BackendConfig;
import com.zurrtum.create.client.flywheel.impl.config.CClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import org.jetbrains.annotations.Nullable;

import static com.zurrtum.create.client.flywheel.impl.Flywheel.MOD_ID;

public class FabricFlwConfig implements FlwConfig {
    public static final FabricFlwConfig INSTANCE = new FabricFlwConfig();
    private CClient client;

    public void register() {
        client = Builder.create(CClient::new, MOD_ID, "client", true);
    }

    @Override
    public Backend backend() {
        Backend backend = parseBackend(client.backend.get());
        if (backend == null) {
            client.backend.set(DEFAULT_BACKEND_STR);
            backend = BackendManager.defaultBackend();
        }

        return backend;
    }

    @Override
    public boolean limitUpdates() {
        return client.limitUpdates.get();
    }

    @Override
    public int workerThreads() {
        return client.workerThreads.get();
    }

    @Override
    public BackendConfig backendConfig() {
        return client.flwBackends;
    }

    @Nullable
    private static Backend parseBackend(String value) {
        if (value.equals(DEFAULT_BACKEND_STR)) {
            return BackendManager.defaultBackend();
        }
        if (value.equals("OFF")) {
            return BackendManager.offBackend();
        }

        Identifier backendId;
        try {
            backendId = Identifier.of(value);
        } catch (InvalidIdentifierException e) {
            FlwImpl.CONFIG_LOGGER.warn("'backend' value '{}' is not a valid resource location", value);
            return null;
        }

        Backend backend = Backend.REGISTRY.get(backendId);
        if (backend == null) {
            FlwImpl.CONFIG_LOGGER.warn("Backend with ID '{}' is not registered", backendId);
            return null;
        }

        return backend;
    }
}
