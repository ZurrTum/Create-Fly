package com.zurrtum.create.client.ponder.api.scene;

import com.zurrtum.create.client.ponder.api.ParticleEmitter;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface EffectInstructions {
    void emitParticles(Vec3d location, ParticleEmitter emitter, float amountPerCycle, int cycles);

    <T extends ParticleEffect> ParticleEmitter simpleParticleEmitter(T data, Vec3d motion);

    <T extends ParticleEffect> ParticleEmitter particleEmitterWithinBlockSpace(T data, Vec3d motion);

    void indicateRedstone(BlockPos pos);

    void indicateSuccess(BlockPos pos);

    void createRedstoneParticles(BlockPos pos, int color, int amount);
}