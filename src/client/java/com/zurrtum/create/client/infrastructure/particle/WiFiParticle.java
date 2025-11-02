package com.zurrtum.create.client.infrastructure.particle;

import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.joml.Quaternionf;

public class WiFiParticle extends CustomRotationParticle {
    private final boolean downward;

    public WiFiParticle(
        SimpleParticleType type,
        SpriteProvider spriteSet,
        ClientWorld worldIn,
        double x,
        double y,
        double z,
        double vx,
        double vy,
        double vz,
        Random random
    ) {
        super(worldIn, x, y + (vy < 0 ? -1 : 1), z, spriteSet, 0);
        this.scale = 0.5f;
        this.setBoundingBoxSpacing(this.scale, this.scale);
        this.loopLength = 16;
        this.maxAge = 16;
        this.updateSprite(spriteSet);
        this.stopped = true; // disable movement
        this.downward = vy < 0;
    }

    @Override
    public void tick() {
        updateSprite(spriteProvider);
        if (age++ >= maxAge)
            markDead();
    }

    @Override
    public Quaternionf getCustomRotation(Camera camera, float partialTicks) {
        Quaternionf rotation = camera.getRotation();
        Quaternionf quaternionf = new Quaternionf(0, rotation.y, 0, rotation.w);
        if (downward) {
            quaternionf.rotateZ(MathHelper.PI);
        }
        return quaternionf;
    }
}
