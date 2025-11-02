package com.zurrtum.create.client.ponder.foundation;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.zurrtum.create.client.catnip.levelWrappers.WrappedClientLevel;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.SubmittableBatch;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.LayeredCustomCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleGroup;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.*;
import java.util.function.Supplier;

public class PonderWorldParticles {
    private static final Frustum FRUSTUM = new PassFrustum();
    private final SubmittableBatch particleBatch = new SubmittableBatch();
    private final Map<ParticleTextureSheet, ParticleRenderer<?>> particles = Maps.newIdentityHashMap();
    private final Queue<Particle> newParticles = Queues.newArrayDeque();
    private final Object2IntOpenHashMap<ParticleGroup> groupCounts = new Object2IntOpenHashMap<>();
    private final ParticleManager particleManager;
    private final LayeredCustomCommandRenderer.VerticesCache verticesCache = new LayeredCustomCommandRenderer.VerticesCache();

    PonderLevel world;
    private final Supplier<ClientWorld> asClientWorld = Suppliers.memoize(() -> WrappedClientLevel.of(world));

    public PonderWorldParticles(PonderLevel world) {
        this.world = world;
        this.particleManager = MinecraftClient.getInstance().particleManager;
    }

    public void addParticle(Particle particle) {
        Optional<ParticleGroup> optional = particle.getGroup();
        if (optional.isPresent()) {
            if (canAdd(optional.get())) {
                newParticles.add(particle);
                addTo(optional.get(), 1);
            }
        } else {
            newParticles.add(particle);
        }
    }

    private boolean canAdd(ParticleGroup group) {
        return groupCounts.getInt(group) < group.maxCount();
    }

    protected void addTo(ParticleGroup group, int count) {
        this.groupCounts.addTo(group, count);
    }

    public void tick() {
        particles.forEach((textureSheet, particlex) -> particlex.tick());

        Particle particle;
        if (!newParticles.isEmpty()) {
            while ((particle = newParticles.poll()) != null) {
                particles.computeIfAbsent(particle.textureSheet(), particleManager::createParticleRenderer).add(particle);
            }
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends ParticleEffect> Particle addParticle(
        T parameters,
        double x,
        double y,
        double z,
        double velocityX,
        double velocityY,
        double velocityZ
    ) {
        ParticleFactory<T> particleFactory = (ParticleFactory<T>) particleManager.spriteManager.getParticleFactories()
            .get(Registries.PARTICLE_TYPE.getRawId(parameters.getType()));
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

    public void renderParticles(
        MatrixStack ms,
        OrderedRenderCommandQueueImpl queue,
        Camera camera,
        CameraRenderState cameraRenderState,
        float tickProgress
    ) {
        Matrix4fStack stack = RenderSystem.getModelViewStack();
        stack.pushMatrix();
        stack.mul(ms.peek().getPositionMatrix());
        for (ParticleTextureSheet particleTextureSheet : ParticleManager.PARTICLE_TEXTURE_SHEETS) {
            ParticleRenderer<?> particleRenderer = particles.get(particleTextureSheet);
            if (particleRenderer != null && !particleRenderer.isEmpty()) {
                particleBatch.add(particleRenderer.render(FRUSTUM, camera, tickProgress));
            }
        }
        particleBatch.submit(queue, cameraRenderState);
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms()
            .write(new Matrix4f(stack), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f(), 0.0F);
        for (BatchingRenderCommandQueue commandQueue : queue.getBatchingQueues().values()) {
            List<OrderedRenderCommandQueue.LayeredCustom> commands = commandQueue.getLayeredCustomCommands();
            if (commands.isEmpty()) {
                continue;
            }
            GpuTextureView gpuTextureView = RenderSystem.outputColorTextureOverride;
            GpuTextureView gpuTextureView2 = RenderSystem.outputDepthTextureOverride;
            MinecraftClient mc = MinecraftClient.getInstance();
            GpuTextureView lightTextureView = mc.gameRenderer.getLightmapTextureManager().getGlTextureView();
            TextureManager textureManager = mc.getTextureManager();
            for (OrderedRenderCommandQueue.LayeredCustom layeredCustom : commands) {
                BillboardParticleSubmittable.Buffers buffers = layeredCustom.submit(verticesCache);
                if (buffers != null) {
                    try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                        () -> "Immediate draw for particle",
                        gpuTextureView,
                        OptionalInt.empty(),
                        gpuTextureView2,
                        OptionalDouble.empty()
                    )) {
                        renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
                        renderPass.setUniform("Projection", RenderSystem.getProjectionMatrixBuffer());
                        renderPass.setUniform("Fog", RenderSystem.getShaderFog());
                        renderPass.bindSampler("Sampler2", lightTextureView);
                        layeredCustom.render(buffers, verticesCache, renderPass, textureManager, false);
                        layeredCustom.render(buffers, verticesCache, renderPass, textureManager, true);
                    }
                }
            }
            commands.clear();
        }
        stack.popMatrix();
        particleBatch.onFrameEnd();
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
        public boolean intersectPoint(double x, double y, double z) {
            return true;
        }
    }
}