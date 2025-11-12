package com.zurrtum.create.client.infrastructure.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Quaternionf;

public class WiFiParticle extends CustomRotationParticle {
    private final boolean downward;

    public WiFiParticle(
        SimpleParticleType type,
        SpriteSet spriteSet,
        ClientLevel worldIn,
        double x,
        double y,
        double z,
        double vx,
        double vy,
        double vz,
        RandomSource random
    ) {
        super(worldIn, x, y + (vy < 0 ? -1 : 1), z, spriteSet, 0);
        this.quadSize = 0.5f;
        this.setSize(this.quadSize, this.quadSize);
        this.loopLength = 16;
        this.lifetime = 16;
        this.setSpriteFromAge(spriteSet);
        this.stoppedByCollision = true; // disable movement
        this.downward = vy < 0;
    }

    @Override
    public void tick() {
        setSpriteFromAge(sprites);
        if (age++ >= lifetime)
            remove();
    }

    @Override
    public Quaternionf getCustomRotation(Camera camera, float partialTicks) {
        Quaternionf rotation = camera.rotation();
        Quaternionf quaternionf = new Quaternionf(0, rotation.y, 0, rotation.w);
        if (downward) {
            quaternionf.rotateZ(Mth.PI);
        }
        return quaternionf;
    }
}
