package com.zurrtum.create.client.flywheel.backend;

import com.zurrtum.create.client.flywheel.api.backend.Backend;
import com.zurrtum.create.client.flywheel.backend.compile.IndirectPrograms;
import com.zurrtum.create.client.flywheel.backend.compile.InstancingPrograms;
import com.zurrtum.create.client.flywheel.backend.engine.EngineImpl;
import com.zurrtum.create.client.flywheel.backend.engine.indirect.IndirectDrawManager;
import com.zurrtum.create.client.flywheel.backend.engine.instancing.InstancedDrawManager;
import com.zurrtum.create.client.flywheel.backend.gl.Driver;
import com.zurrtum.create.client.flywheel.backend.gl.GlCompat;
import com.zurrtum.create.client.flywheel.backend.vulkan.VulkanDrawManager;
import com.zurrtum.create.client.flywheel.backend.vulkan.VulkanSupport;
import com.zurrtum.create.client.flywheel.lib.backend.SimpleBackend;
import com.zurrtum.create.client.flywheel.lib.util.ResourceUtil;
import com.zurrtum.create.client.flywheel.lib.util.ShadersModHelper;

public final class Backends {
    /**
     * Use GPU instancing to render everything.
     */
    public static final Backend INSTANCING = SimpleBackend.builder()
        .engineFactory((level) -> new EngineImpl(level, new InstancedDrawManager(InstancingPrograms.get()), 256))
        .priority(500)
        .supported(() -> GlCompat.SUPPORTS_INSTANCING && InstancingPrograms.allLoaded() && !ShadersModHelper.isShaderPackInUse())
        .register(ResourceUtil.rl("instancing"));
    /**
     * Use Compute shaders to cull instances.
     */
    public static final Backend INDIRECT = SimpleBackend.builder()
        .engineFactory((level) -> new EngineImpl(level, new IndirectDrawManager(IndirectPrograms.get()), 256))
        .priority(() -> GlCompat.DRIVER == Driver.INTEL ? 1 : 1000)
        .supported(() -> GlCompat.SUPPORTS_INDIRECT && IndirectPrograms.allLoaded() && !ShadersModHelper.isShaderPackInUse())
        .register(ResourceUtil.rl("indirect"));

    /**
     * Experimental GPU-backend port that talks to the device abstraction exposed by
     * {@code RenderSystem.getDevice()} instead of raw OpenGL.
     *
     * <p>The three original backends are untouched and remain selectable through
     * {@code flywheel-client.json} -> {@code backend}.
     *
     * <p>Priority deliberately equals {@link #INDIRECT}'s (1000) rather than exceeding it. With
     * {@code List.sort} being stable and {@code REGISTRY.getAll()} returning registration order,
     * that puts the chain in the order INDIRECT, VULKAN, INSTANCING, OFF - i.e. this backend
     * competes on equal footing instead of jumping the queue, so Flywheel's own selection logic
     * does a real comparison. Note INDIRECT drops to priority 1 on Intel drivers, in which case
     * this backend does sort first.
     *
     * <p>Phase 3: {@link VulkanSupport#probe()} returns {@code false} on purpose, so the chain
     * walks past it and lands on {@code flywheel:off}. Its absence would be silent, which is why
     * probe() logs every time it is consulted. Flip the return once {@link VulkanDrawManager}
     * can actually submit draws.
     */
    public static final Backend VULKAN = SimpleBackend.builder()
        .engineFactory(level -> new EngineImpl(level, new VulkanDrawManager(level), 256))
        .priority(1000)
        .supported(() -> VulkanSupport.probe())
        .register(ResourceUtil.rl("vulkan"));

    private Backends() {
    }

    public static void init() {
    }
}
