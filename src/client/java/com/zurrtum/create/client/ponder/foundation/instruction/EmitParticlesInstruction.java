package com.zurrtum.create.client.ponder.foundation.instruction;

import com.zurrtum.create.client.ponder.api.ParticleEmitter;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class EmitParticlesInstruction extends TickingInstruction {

    private final Vec3d anchor;
    private final ParticleEmitter emitter;
    private final float runsPerTick;

    public EmitParticlesInstruction(Vec3d anchor, ParticleEmitter emitter, float runsPerTick, int ticks) {
        super(false, ticks);
        this.anchor = anchor;
        this.emitter = emitter;
        this.runsPerTick = runsPerTick;
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);
        int runs = (int) runsPerTick;
        PonderLevel world = scene.getWorld();
        Random random = world.random;
        if (random.nextFloat() < (runsPerTick - runs))
            runs++;
        for (int i = 0; i < runs; i++)
            emitter.create(world, anchor.x, anchor.y, anchor.z);
    }

}