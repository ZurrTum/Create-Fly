package com.zurrtum.create.client.flywheel.api.backend;

import com.zurrtum.create.client.flywheel.api.internal.FlwApiLink;
import com.zurrtum.create.client.flywheel.api.registry.IdRegistry;
import net.minecraft.world.WorldAccess;

@BackendImplemented
public interface Backend {
    IdRegistry<Backend> REGISTRY = FlwApiLink.INSTANCE.createIdRegistry();

    /**
     * Create a new engine instance.
     */
    Engine createEngine(WorldAccess var1);

    /**
     * The priority of this backend.
     * <p>The backend with the highest priority upon first launch will be chosen as the default backend.
     *
     * <p>If the selected backend becomes unavailable for whatever reason, the next supported backend
     * with a LOWER priority than the selected one will be chosen.
     *
     * @return The priority of this backend.
     */
    int priority();

    /**
     * Check if this backend is supported.
     */
    boolean isSupported();
}
