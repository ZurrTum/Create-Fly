package com.zurrtum.create.client.infrastructure.particle;

import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;

public class WiFiParticle extends CustomRotationParticle {

    private boolean downward;

    public WiFiParticle(
        SimpleParticleType type,
        SpriteProvider spriteSet,
        ClientWorld worldIn,
        double x,
        double y,
        double z,
        double vx,
        double vy,
        double vz
    ) {
        super(worldIn, x, y + (vy < 0 ? -1 : 1), z, spriteSet, 0);
        this.scale = 0.5f;
        this.setBoundingBoxSpacing(this.scale, this.scale);
        this.loopLength = 16;
        this.maxAge = 16;
        this.setSpriteForAge(spriteSet);
        this.stopped = true; // disable movement
        this.downward = vy < 0;
    }

    @Override
    public void tick() {
        setSpriteForAge(spriteProvider);
        if (age++ >= maxAge)
            markDead();
    }

    @Override
    public Quaternionf getCustomRotation(Camera camera, float partialTicks) {
        return new Quaternionf().rotateY(-camera.getYaw() * MathHelper.RADIANS_PER_DEGREE)
            .mul(new Quaternionf().rotateZ(downward ? MathHelper.PI : 0));
    }
}
