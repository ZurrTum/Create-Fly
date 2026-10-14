package com.zurrtum.create.client.flywheel.backend.vulkan;

import com.zurrtum.create.client.flywheel.api.backend.Engine;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.backend.FlwBackend;
import com.zurrtum.create.client.flywheel.backend.engine.AbstractInstancer;
import com.zurrtum.create.client.flywheel.backend.engine.DrawManager;
import com.zurrtum.create.client.flywheel.backend.engine.InstancerKey;
import com.zurrtum.create.client.flywheel.backend.engine.LightStorage;
import com.zurrtum.create.client.flywheel.backend.engine.MeshPool;
import com.zurrtum.create.client.flywheel.backend.engine.embed.EnvironmentStorage;
import com.zurrtum.create.client.flywheel.backend.engine.instancing.InstancedInstancer;

import net.minecraft.world.level.LevelAccessor;

import java.util.List;

/**
 * Phase 3 skeleton: registered as {@code flywheel:vulkan} but deliberately never draws anything.
 *
 * <p>Its only job right now is to prove the backend gets registered, sorts first in the priority
 * chain, and gets picked/fallen-back correctly by {@code BackendManagerImpl}. Everything here
 * logs instead of rendering.
 *
 * <p>It reuses the real {@link InstancedInstancer} and {@link MeshPool} rather than stubs so the
 * type relationships stay honest, but nothing is uploaded to the GPU.
 *
 * <p>What a real implementation needs on top of this - see {@link InstancedDrawManager} for the
 * reference implementation:
 * <ul>
 *   <li>{@code GlVertexArray} and {@code TextureBuffer} for instance data -&gt; {@code GpuBuffer} /
 *       {@code setVertexBuffer} / {@code bindTexture} instead.</li>
 *   <li>{@code EngineImpl.render()} wraps the draw in {@code GlStateTracker.getRestoreState()}.</li>
 *   <li>Submission goes through
 *       {@code programs.get(instanceType, contextShader, material, oitMode)} - a four-way shader
 *       selection that is the main cost centre.</li>
 *   <li>OIT is a five-pass sequence over an {@code OitFramebuffer}. Plan is to skip it in v1.</li>
 * </ul>
 */
public class VulkanDrawManager extends DrawManager<InstancedInstancer<?>> {

    private final MeshPool meshPool = new MeshPool();

    public VulkanDrawManager(LevelAccessor level) {
        FlwBackend.LOGGER.info("[FW/VK] VulkanDrawManager constructed");
    }

    @Override
    public MeshPool meshPool() {
        return meshPool;
    }

    @Override
    public void render(LightStorage lightStorage, EnvironmentStorage environmentStorage) {
        int total = 0;
        for (InstancedInstancer<?> instancer : instancers.values()) {
            total += instancer.instanceCount();
        }
        FlwBackend.LOGGER.info("[FW/VK] render(): instancers={} totalInstances={}",
            instancers.size(), total);

        // Drain the initialisation queue exactly like the real backends do - that part is
        // backend-agnostic and worth exercising even in the skeleton.
        super.render(lightStorage, environmentStorage);
    }

    @Override
    protected <I extends Instance> InstancedInstancer<I> create(InstancerKey<I> key) {
        FlwBackend.LOGGER.info("[FW/VK] create() type={} env={} bias={} meshes={}",
            key.type(), key.environment(), key.bias(),
            key.model() == null ? "?" : key.model().meshes().size());
        return new InstancedInstancer<>(key, new AbstractInstancer.Recreate<>(key, this));
    }

    @Override
    protected <I extends Instance> void initialize(InstancerKey<I> key, InstancedInstancer<?> instancer) {
        FlwBackend.LOGGER.info("[FW/VK] initialize() key={}", key);
    }

    @Override
    public void renderCrumbling(List<Engine.CrumblingBlock> crumblingBlocks) {
        FlwBackend.LOGGER.info("[FW/VK] renderCrumbling(): {} blocks", crumblingBlocks.size());
    }

    @Override
    public void triggerFallback() {
        FlwBackend.LOGGER.info("[FW/VK] triggerFallback()");
    }

    @Override
    public void delete() {
        FlwBackend.LOGGER.info("[FW/VK] delete()");
        super.delete();
    }
}