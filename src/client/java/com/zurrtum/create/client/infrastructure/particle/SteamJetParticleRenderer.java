package com.zurrtum.create.client.infrastructure.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.QuadParticleGroup;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import org.joml.Quaternionf;

public class SteamJetParticleRenderer extends QuadParticleGroup {
    public static final ParticleRenderType SHEET = new ParticleRenderType("create:steam_jet");

    public SteamJetParticleRenderer(ParticleEngine manager) {
        super(manager, SHEET);
        particleTypeRenderState = new SteamJetParticleSubmittable();
    }

    public static class SteamJetParticleSubmittable extends QuadParticleRenderState {
        @Override
        protected void renderRotatedQuad(
            VertexConsumer vertexConsumer,
            float x,
            float y,
            float z,
            float rotationX,
            float rotationY,
            float rotationZ,
            float rotationW,
            float size,
            float minU,
            float maxU,
            float minV,
            float maxV,
            int color,
            int brightness
        ) {
            Quaternionf quaternionf = new Quaternionf(rotationX, rotationY, rotationZ, rotationW);
            renderVertex(vertexConsumer, quaternionf, x, y, z, 1.0F, 0, size, maxU, maxV, color, brightness);
            renderVertex(vertexConsumer, quaternionf, x, y, z, 1.0F, 2.0F, size, maxU, minV, color, brightness);
            renderVertex(vertexConsumer, quaternionf, x, y, z, -1.0F, 2.0F, size, minU, minV, color, brightness);
            renderVertex(vertexConsumer, quaternionf, x, y, z, -1.0F, 0, size, minU, maxV, color, brightness);
        }
    }
}
