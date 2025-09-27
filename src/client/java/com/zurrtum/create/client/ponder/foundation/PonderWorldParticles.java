package com.zurrtum.create.client.ponder.foundation;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4fStack;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;

public class PonderWorldParticles {

    private final Map<ParticleTextureSheet, Queue<Particle>> byType = Maps.newIdentityHashMap();
    private final Queue<Particle> queue = Queues.newArrayDeque();

    PonderLevel world;

    public PonderWorldParticles(PonderLevel world) {
        this.world = world;
    }

    public void addParticle(Particle p) {
        this.queue.add(p);
    }

    public void tick() {
        this.byType.forEach((p_228347_1_, p_228347_2_) -> this.tickParticleList(p_228347_2_));

        Particle particle;
        if (queue.isEmpty())
            return;
        while ((particle = this.queue.poll()) != null)
            this.byType.computeIfAbsent(particle.getType(), $ -> EvictingQueue.create(16384)).add(particle);
    }

    private void tickParticleList(Collection<Particle> p_187240_1_) {
        if (p_187240_1_.isEmpty())
            return;

        Iterator<Particle> iterator = p_187240_1_.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            particle.tick();
            if (!particle.isAlive())
                iterator.remove();
        }
    }

    public void renderParticles(MatrixStack ms, SuperRenderTypeBuffer buffer, Camera renderInfo, float pt) {
        buffer.draw();

        Matrix4fStack stack = RenderSystem.getModelViewStack();
        stack.pushMatrix();
        stack.mul(ms.peek().getPositionMatrix());

        for (ParticleTextureSheet iparticlerendertype : this.byType.keySet()) {
            if (iparticlerendertype == ParticleTextureSheet.NO_RENDER)
                continue;
            Iterable<Particle> iterable = this.byType.get(iparticlerendertype);
            if (iterable != null) {
                RenderLayer layer = iparticlerendertype.renderType();
                if (layer != null) {
                    VertexConsumer vertexConsumer = buffer.getBuffer(layer);
                    for (Particle particle : iterable)
                        particle.render(vertexConsumer, renderInfo, pt);
                } else {
                    for (Particle particle : iterable)
                        particle.renderCustom(ms, buffer, renderInfo, pt);
                }
            }
        }

        buffer.draw();
        stack.popMatrix();
    }

    public void clearEffects() {
        this.byType.clear();
    }

}