package com.zurrtum.create.client.ponder.foundation;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.levelWrappers.WrappedClientLevel;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.feature.ParticleFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.state.ParticlesRenderState;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.ParticleLimit;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.util.*;
import java.util.function.Supplier;

public class PonderWorldParticles {
    private static final Frustum FRUSTUM = new PassFrustum();
    private final ParticlesRenderState particleBatch = new ParticlesRenderState();
    private final Map<ParticleRenderType, ParticleGroup<?>> particles = Maps.newIdentityHashMap();
    private final Queue<Particle> newParticles = Queues.newArrayDeque();
    private final Object2IntOpenHashMap<ParticleLimit> groupCounts = new Object2IntOpenHashMap<>();
    private final ParticleEngine particleManager;
    private final ParticleFeatureRenderer.ParticleBufferCache verticesCache = new ParticleFeatureRenderer.ParticleBufferCache();

    PonderLevel world;
    private final Supplier<ClientLevel> asClientWorld = Suppliers.memoize(() -> WrappedClientLevel.of(world));

    public PonderWorldParticles(PonderLevel world) {
        this.world = world;
        this.particleManager = Minecraft.getInstance().particleEngine;
    }

    public void addParticle(Particle particle) {
        Optional<ParticleLimit> optional = particle.getParticleLimit();
        if (optional.isPresent()) {
            if (canAdd(optional.get())) {
                newParticles.add(particle);
                addTo(optional.get(), 1);
            }
        } else {
            newParticles.add(particle);
        }
    }

    private boolean canAdd(ParticleLimit group) {
        return groupCounts.getInt(group) < group.limit();
    }

    protected void addTo(ParticleLimit group, int count) {
        this.groupCounts.addTo(group, count);
    }

    public void tick() {
        particles.forEach((textureSheet, particlex) -> particlex.tickParticles());

        Particle particle;
        if (!newParticles.isEmpty()) {
            while ((particle = newParticles.poll()) != null) {
                particles.computeIfAbsent(particle.getGroup(), particleManager::createParticleGroup).add(particle);
            }
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends ParticleOptions> Particle addParticle(
        T parameters,
        double x,
        double y,
        double z,
        double velocityX,
        double velocityY,
        double velocityZ
    ) {
        ParticleProvider<T> particleFactory = (ParticleProvider<T>) particleManager.resourceManager.getProviders()
            .get(BuiltInRegistries.PARTICLE_TYPE.getId(parameters.getType()));
        if (particleFactory == null) {
            return null;
        }
        Particle particle = particleFactory.createParticle(parameters, asClientWorld.get(), x, y, z, velocityX, velocityY, velocityZ, world.random);
        if (particle == null) {
            return null;
        }
        addParticle(particle);
        return particle;
    }

    public void renderParticles(PoseStack ms, SubmitNodeStorage queue, Camera camera, CameraRenderState cameraRenderState, float tickProgress) {
        Matrix4fStack stack = RenderSystem.getModelViewStack();
        stack.pushMatrix();
        stack.mul(ms.last().pose());
        for (ParticleRenderType particleTextureSheet : ParticleEngine.RENDER_ORDER) {
            ParticleGroup<?> particleRenderer = particles.get(particleTextureSheet);
            if (particleRenderer != null && !particleRenderer.isEmpty()) {
                particleBatch.add(particleRenderer.extractRenderState(FRUSTUM, camera, tickProgress));
            }
        }
        particleBatch.submit(queue, cameraRenderState);
        for (SubmitNodeCollection commandQueue : queue.getSubmitsPerOrder().values()) {
            List<SubmitNodeCollector.ParticleGroupRenderer> commands = commandQueue.getParticleGroupRenderers();
            if (commands.isEmpty()) {
                continue;
            }
            GpuTextureView gpuTextureView = RenderSystem.outputColorTextureOverride;
            GpuTextureView gpuTextureView2 = RenderSystem.outputDepthTextureOverride;
            Minecraft mc = Minecraft.getInstance();
            GpuTextureView lightTextureView = mc.gameRenderer.lightTexture().getTextureView();
            GpuSampler sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
            TextureManager textureManager = mc.getTextureManager();
            for (SubmitNodeCollector.ParticleGroupRenderer layeredCustom : commands) {
                QuadParticleRenderState.PreparedBuffers buffers = layeredCustom.prepare(verticesCache);
                if (buffers != null) {
                    try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                        () -> "Immediate draw for particle",
                        gpuTextureView,
                        OptionalInt.empty(),
                        gpuTextureView2,
                        OptionalDouble.empty()
                    )) {
                        renderPass.setUniform("Projection", RenderSystem.getProjectionMatrixBuffer());
                        renderPass.setUniform("Fog", RenderSystem.getShaderFog());
                        renderPass.bindTexture("Sampler2", lightTextureView, sampler);
                        layeredCustom.render(buffers, verticesCache, renderPass, textureManager, false);
                        layeredCustom.render(buffers, verticesCache, renderPass, textureManager, true);
                    }
                }
            }
            commands.clear();
        }
        stack.popMatrix();
        particleBatch.reset();
    }

    public void clearEffects() {
        newParticles.clear();
        groupCounts.clear();
    }

    public static class PassFrustum extends Frustum {
        public PassFrustum() {
            super(new Matrix4f(), new Matrix4f());
        }

        @Override
        public boolean pointInFrustum(double x, double y, double z) {
            return true;
        }
    }
}