package com.zurrtum.create.content.kinetics.base;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.base.IRotate.SpeedLevel;
import com.zurrtum.create.infrastructure.particle.RotationIndicatorParticleData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class KineticEffectHandler {

    int overStressedTime;
    public float overStressedEffect;
    int particleSpawnCountdown;
    KineticBlockEntity kte;

    public KineticEffectHandler(KineticBlockEntity kte) {
        this.kte = kte;
    }

    public void tick() {
        World world = kte.getWorld();

        if (world.isClient) {
            if (overStressedTime > 0)
                if (--overStressedTime == 0)
                    if (kte.isOverStressed()) {
                        overStressedEffect = 1;
                        spawnEffect(ParticleTypes.SMOKE, 0.2f, 5);
                    } else {
                        overStressedEffect = -1;
                        spawnEffect(ParticleTypes.CLOUD, .075f, 2);
                    }

            if (overStressedEffect != 0) {
                overStressedEffect -= overStressedEffect * .1f;
                if (Math.abs(overStressedEffect) < 1 / 128f)
                    overStressedEffect = 0;
            }

        } else if (particleSpawnCountdown > 0) {
            if (--particleSpawnCountdown == 0)
                spawnRotationIndicators();
        }
    }

    public void queueRotationIndicators() {
        particleSpawnCountdown = 2;
    }

    public void spawnEffect(ParticleEffect particle, float maxMotion, int amount) {
        World world = kte.getWorld();
        if (world == null)
            return;
        if (!world.isClient)
            return;
        Random r = world.random;
        for (int i = 0; i < amount; i++) {
            Vec3d motion = VecHelper.offsetRandomly(Vec3d.ZERO, r, maxMotion);
            Vec3d position = VecHelper.getCenterOf(kte.getPos());
            world.addParticleClient(particle, position.x, position.y, position.z, motion.x, motion.y, motion.z);
        }
    }

    public void spawnRotationIndicators() {
        float speed = kte.getSpeed();
        if (speed == 0)
            return;

        BlockState state = kte.getCachedState();
        Block block = state.getBlock();
        if (!(block instanceof KineticBlock kb))
            return;

        float radius1 = kb.getParticleInitialRadius();
        float radius2 = kb.getParticleTargetRadius();

        Axis axis = kb.getRotationAxis(state);
        BlockPos pos = kte.getPos();
        World world = kte.getWorld();
        if (axis == null)
            return;
        if (world == null)
            return;

        Vec3d vec = VecHelper.getCenterOf(pos);
        SpeedLevel speedLevel = SpeedLevel.of(speed);
        int color = speedLevel.getColor();
        int particleSpeed = speedLevel.getParticleSpeed();
        particleSpeed *= Math.signum(speed);

        if (world instanceof ServerWorld serverWorld) {
            RotationIndicatorParticleData particleData = new RotationIndicatorParticleData(color, particleSpeed, radius1, radius2, 10, axis);
            serverWorld.spawnParticles(particleData, vec.x, vec.y, vec.z, 20, 0, 0, 0, 1);
        }
    }

    public void triggerOverStressedEffect() {
        overStressedTime = overStressedTime == 0 ? 2 : 0;
    }

}