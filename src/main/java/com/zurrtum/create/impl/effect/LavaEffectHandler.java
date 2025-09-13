package com.zurrtum.create.impl.effect;

import com.zurrtum.create.api.effect.OpenPipeEffectHandler;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class LavaEffectHandler implements OpenPipeEffectHandler {
    @Override
    public void apply(World level, Box area, FluidStack fluid) {
        if (level.getTime() % 5 != 0)
            return;

        List<Entity> entities = level.getOtherEntities(null, area, entity -> !entity.isFireImmune());
        for (Entity entity : entities) {
            entity.setOnFireFor(3);
        }
    }
}
