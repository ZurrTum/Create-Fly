package com.zurrtum.create.client.ponder.foundation.instruction;

import com.zurrtum.create.client.ponder.api.ParticleEmitter;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class EmitParticlesInstruction extends TickingInstruction {

    private final Vec3 anchor;
    private final ParticleEmitter emitter;
    private final float runsPerTick;

    public EmitParticlesInstruction(Vec3 anchor, ParticleEmitter emitter, float runsPerTick, int ticks) {
        super(false, ticks);
        this.anchor = anchor;
        this.emitter = emitter;
        this.runsPerTick = runsPerTick;
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);
        int runs = (int) runsPerTick;
        PonderLevel world = scene.getLevel();
        RandomSource random = world.getRandom();
        if (random.nextFloat() < (runsPerTick - runs))
            runs++;
        for (int i = 0; i < runs; i++)
            emitter.create(world, anchor.x, anchor.y, anchor.z);
    }

}