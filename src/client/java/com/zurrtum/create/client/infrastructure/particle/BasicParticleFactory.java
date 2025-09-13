package com.zurrtum.create.client.infrastructure.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

public class BasicParticleFactory implements ParticleFactory<SimpleParticleType> {
    @FunctionalInterface
    public interface Factory {
        Particle createParticle(
            SimpleParticleType parameters,
            SpriteProvider spriteSet,
            ClientWorld world,
            double x,
            double y,
            double z,
            double velocityX,
            double velocityY,
            double velocityZ
        );
    }

    private final Factory factory;
    private SpriteProvider spriteSet;

    private BasicParticleFactory(Factory factory, SpriteProvider spriteSet) {
        this.factory = factory;
        this.spriteSet = spriteSet;
    }

    public static ParticleFactory<SimpleParticleType> wifi(SpriteProvider spriteSet) {
        return new BasicParticleFactory(WiFiParticle::new, spriteSet);
    }

    public static ParticleFactory<SimpleParticleType> soul(SpriteProvider spriteSet) {
        return new BasicParticleFactory(SoulParticle::new, spriteSet);
    }

    public static ParticleFactory<SimpleParticleType> soulBase(SpriteProvider spriteSet) {
        return new BasicParticleFactory(SoulBaseParticle::new, spriteSet);
    }

    @Override
    public Particle createParticle(
        SimpleParticleType parameters,
        ClientWorld world,
        double x,
        double y,
        double z,
        double velocityX,
        double velocityY,
        double velocityZ
    ) {
        return factory.createParticle(parameters, spriteSet, world, x, y, z, velocityX, velocityY, velocityZ);
    }
}
