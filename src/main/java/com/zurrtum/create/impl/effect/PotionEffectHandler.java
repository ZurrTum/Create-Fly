package com.zurrtum.create.impl.effect;

import com.zurrtum.create.api.effect.OpenPipeEffectHandler;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.infrastructure.fluids.BottleFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class PotionEffectHandler implements OpenPipeEffectHandler {
    @Override
    public void apply(World level, Box area, FluidStack fluid) {
        PotionContentsComponent contents = getContents(fluid);
        if (contents == PotionContentsComponent.DEFAULT)
            return;

        List<LivingEntity> entities = level.getEntitiesByClass(LivingEntity.class, area, LivingEntity::isAffectedBySplashPotions);
        for (LivingEntity entity : entities) {
            contents.forEachEffect(
                effectInstance -> {
                    StatusEffect effect = effectInstance.getEffectType().value();
                    if (effect.isInstant()) {
                        if (level instanceof ServerWorld serverWorld) {
                            effect.applyInstantEffect(serverWorld, null, null, entity, effectInstance.getAmplifier(), 0.5D);
                        }
                    } else {
                        entity.addStatusEffect(new StatusEffectInstance(effectInstance));
                    }
                }, 1
            );
        }
    }

    private static PotionContentsComponent getContents(FluidStack fluid) {
        FluidStack copy = fluid.copy();
        copy.setAmount(BottleFluidInventory.CAPACITY);
        ItemStack bottle = PotionFluidHandler.fillBottle(new ItemStack(Items.GLASS_BOTTLE), copy);
        return bottle.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
    }
}
