package com.zurrtum.create.client.flywheel.backend;

import com.zurrtum.create.client.flywheel.api.backend.Backend;
import com.zurrtum.create.client.flywheel.backend.compile.IndirectPrograms;
import com.zurrtum.create.client.flywheel.backend.compile.InstancingPrograms;
import com.zurrtum.create.client.flywheel.backend.engine.EngineImpl;
import com.zurrtum.create.client.flywheel.backend.engine.indirect.IndirectDrawManager;
import com.zurrtum.create.client.flywheel.backend.engine.instancing.InstancedDrawManager;
import com.zurrtum.create.client.flywheel.backend.gl.Driver;
import com.zurrtum.create.client.flywheel.backend.gl.GlCompat;
import com.zurrtum.create.client.flywheel.lib.backend.SimpleBackend;
import com.zurrtum.create.client.flywheel.lib.util.ResourceUtil;
import com.zurrtum.create.client.flywheel.lib.util.ShadersModHelper;

public final class Backends {
    public static final Backend INSTANCING = SimpleBackend.builder()
        .engineFactory((level) -> new EngineImpl(level, new InstancedDrawManager(InstancingPrograms.get()), 256)).priority(500)
        .supported(() -> GlCompat.SUPPORTS_INSTANCING && InstancingPrograms.allLoaded() && !ShadersModHelper.isShaderPackInUse())
        .register(ResourceUtil.rl("instancing"));
    public static final Backend INDIRECT = SimpleBackend.builder()
        .engineFactory((level) -> new EngineImpl(level, new IndirectDrawManager(IndirectPrograms.get()), 256))
        .priority(() -> GlCompat.DRIVER == Driver.INTEL ? 1 : 1000)
        .supported(() -> GlCompat.SUPPORTS_INDIRECT && IndirectPrograms.allLoaded() && !ShadersModHelper.isShaderPackInUse())
        .register(ResourceUtil.rl("indirect"));

    private Backends() {
    }

    public static void init() {
    }
}
