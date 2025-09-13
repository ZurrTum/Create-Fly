package com.zurrtum.create.client.infrastructure.particle;

import com.zurrtum.create.client.content.equipment.bell.SoulPulseEffect;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;

public class SoulBaseParticle extends CustomRotationParticle {

    public SoulBaseParticle(
        SimpleParticleType parameters,
        SpriteProvider spriteSet,
        ClientWorld worldIn,
        double x,
        double y,
        double z,
        double vx,
        double vy,
        double vz
    ) {
        super(worldIn, x, y, z, spriteSet, 0);
        this.scale = 0.5f;
        this.setBoundingBoxSpacing(this.scale, this.scale);
        this.loopLength = 16 + (int) (this.random.nextFloat() * 2f - 1f);
        this.maxAge = (int) (90.0F / (this.random.nextFloat() * 0.36F + 0.64F));
        this.selectSpriteLoopingWithAge(spriteProvider);
        this.stopped = true; // disable movement
    }

    @Override
    public void tick() {
        selectSpriteLoopingWithAge(spriteProvider);

        BlockPos pos = BlockPos.ofFloored(x, y, z);
        if (age++ >= maxAge || !SoulPulseEffect.isDark(world, pos))
            markDead();
    }

    @Override
    public Quaternionf getCustomRotation(Camera camera, float partialTicks) {
        return RotationAxis.POSITIVE_X.rotationDegrees(90);
    }
}
