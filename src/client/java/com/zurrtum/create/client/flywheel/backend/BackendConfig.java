package com.zurrtum.create.client.flywheel.backend;

import com.zurrtum.create.client.flywheel.backend.compile.LightSmoothness;

public interface BackendConfig {
    BackendConfig INSTANCE = FlwBackend.config();

    LightSmoothness lightSmoothness();
}
