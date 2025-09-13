package com.zurrtum.create.impl.effect;

import com.zurrtum.create.api.effect.OpenPipeEffectHandler;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class TeaEffectHandler implements OpenPipeEffectHandler {
    @Override
    public void apply(World level, Box area, FluidStack fluid) {
        if (level.getTime() % 5 != 0)
            return;

        List<LivingEntity> entities = level.getEntitiesByClass(LivingEntity.class, area, LivingEntity::isAffectedBySplashPotions);
        for (LivingEntity entity : entities) {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 21, 0, false, false, false));
        }
    }
}
