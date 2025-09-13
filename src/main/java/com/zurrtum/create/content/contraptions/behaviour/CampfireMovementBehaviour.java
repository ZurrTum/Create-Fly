package com.zurrtum.create.content.contraptions.behaviour;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import net.minecraft.block.CampfireBlock;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.random.Random;

public class CampfireMovementBehaviour extends MovementBehaviour {
    @Override
    public void tick(MovementContext context) {
        if (context.world == null || !context.world.isClient || context.position == null || !context.state.get(CampfireBlock.LIT) || context.disabled)
            return;

        // Mostly copied from CampfireBlock and CampfireBlockEntity
        Random random = context.world.random;
        if (random.nextFloat() < 0.11F) {
            for (int i = 0; i < random.nextInt(2) + 2; ++i) {
                context.world.addImportantParticleClient(
                    context.state.get(CampfireBlock.SIGNAL_FIRE) ? ParticleTypes.CAMPFIRE_SIGNAL_SMOKE : ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    true,
                    context.position.getX() + random.nextDouble() / (random.nextBoolean() ? 3D : -3D),
                    context.position.getY() + random.nextDouble() + random.nextDouble(),
                    context.position.getZ() + random.nextDouble() / (random.nextBoolean() ? 3D : -3D),
                    0.0D,
                    0.07D,
                    0.0D
                );
            }
        }
    }
}