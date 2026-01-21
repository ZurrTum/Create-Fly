package com.zurrtum.create.client.infrastructure.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class BasicParticleFactory implements ParticleProvider<SimpleParticleType> {
    @FunctionalInterface
    public interface Factory {
        Particle createParticle(
            SimpleParticleType parameters,
            SpriteSet spriteSet,
            ClientLevel world,
            double x,
            double y,
            double z,
            double velocityX,
            double velocityY,
            double velocityZ,
            RandomSource random
        );
    }

    private final Factory factory;
    private final SpriteSet spriteSet;

    private BasicParticleFactory(Factory factory, SpriteSet spriteSet) {
        this.factory = factory;
        this.spriteSet = spriteSet;
    }

    public static ParticleProvider<SimpleParticleType> wifi(SpriteSet spriteSet) {
        return new BasicParticleFactory(WiFiParticle::new, spriteSet);
    }

    public static ParticleProvider<SimpleParticleType> soul(SpriteSet spriteSet) {
        return new BasicParticleFactory(SoulParticle::new, spriteSet);
    }

    public static ParticleProvider<SimpleParticleType> soulBase(SpriteSet spriteSet) {
        return new BasicParticleFactory(SoulBaseParticle::new, spriteSet);
    }

    @Override
    public Particle createParticle(
        SimpleParticleType parameters,
        ClientLevel world,
        double x,
        double y,
        double z,
        double velocityX,
        double velocityY,
        double velocityZ,
        RandomSource random
    ) {
        return factory.createParticle(parameters, spriteSet, world, x, y, z, velocityX, velocityY, velocityZ, random);
    }
}
