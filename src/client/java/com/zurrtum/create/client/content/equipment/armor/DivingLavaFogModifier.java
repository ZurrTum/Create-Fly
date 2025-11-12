package com.zurrtum.create.client.content.equipment.armor;

import com.zurrtum.create.content.equipment.armor.DivingHelmetItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.LavaFogEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class DivingLavaFogModifier extends LavaFogEnvironment {
    @Override
    public void setupFog(
        FogData data,
        Entity cameraEntity,
        BlockPos cameraPos,
        ClientLevel world,
        float viewDistance,
        DeltaTracker tickCounter
    ) {
        super.setupFog(data, cameraEntity, cameraPos, world, viewDistance, tickCounter);
        if (cameraEntity.isSpectator()) {
            return;
        }
        ItemStack divingHelmet = DivingHelmetItem.getWornItem(cameraEntity);
        if (divingHelmet.isEmpty() || divingHelmet.canBeHurtBy(world.damageSources().lava())) {
            return;
        }
        data.environmentalStart = -4.0f;
        data.environmentalEnd = 20.0f;
    }
}
