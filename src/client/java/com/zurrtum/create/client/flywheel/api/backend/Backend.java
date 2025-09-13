package com.zurrtum.create.client.flywheel.api.backend;

import com.zurrtum.create.client.flywheel.api.internal.FlwApiLink;
import com.zurrtum.create.client.flywheel.api.registry.IdRegistry;
import net.minecraft.world.WorldAccess;

@BackendImplemented
public interface Backend {
    IdRegistry<Backend> REGISTRY = FlwApiLink.INSTANCE.createIdRegistry();

    Engine createEngine(WorldAccess var1);

    int priority();

    boolean isSupported();
}
