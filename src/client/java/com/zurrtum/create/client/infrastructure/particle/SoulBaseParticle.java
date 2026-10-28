package com.zurrtum.create.client.infrastructure.particle;

import com.mojang.math.Axis;
import com.zurrtum.create.client.content.equipment.bell.SoulPulseEffect;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import org.joml.Quaternionf;

public class SoulBaseParticle extends CustomRotationParticle {

    public SoulBaseParticle(
        SimpleParticleType parameters,
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
        super(worldIn, x, y, z, spriteSet, 0);
        quadSize = 0.5f;
        setSize(quadSize, quadSize);
        loopLength = 16 + (int) (random.nextFloat() * 2.0f - 1.0f);
        lifetime = (int) (90.0F / (random.nextFloat() * 0.36F + 0.64F));
        selectSpriteLoopingWithAge(sprites);
        stoppedByCollision = true; // disable movement
    }

    @Override
    public void tick() {
        selectSpriteLoopingWithAge(sprites);

        BlockPos pos = BlockPos.containing(x, y, z);
        if (age++ >= lifetime || !SoulPulseEffect.isDark(level, pos)) {
            remove();
        }
    }

    @Override
    public Quaternionf getCustomRotation(Camera camera, float partialTicks) {
        return Axis.XP.rotation(Radians.DEG_270);
    }
}
