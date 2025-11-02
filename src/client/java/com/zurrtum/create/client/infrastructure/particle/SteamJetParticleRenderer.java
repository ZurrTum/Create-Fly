package com.zurrtum.create.client.infrastructure.particle;

import net.minecraft.client.particle.BillboardParticleRenderer;
import net.minecraft.client.particle.BillboardParticleSubmittable;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.VertexConsumer;
import org.joml.Quaternionf;

public class SteamJetParticleRenderer extends BillboardParticleRenderer {
    public static final ParticleTextureSheet SHEET = new ParticleTextureSheet("create:steam_jet");

    public SteamJetParticleRenderer(ParticleManager manager) {
        super(manager, SHEET);
        submittable = new SteamJetParticleSubmittable();
    }

    public static class SteamJetParticleSubmittable extends BillboardParticleSubmittable {
        @Override
        protected void drawFace(
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
