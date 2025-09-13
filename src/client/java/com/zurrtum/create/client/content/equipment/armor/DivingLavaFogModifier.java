package com.zurrtum.create.client.content.equipment.armor;

import com.zurrtum.create.content.equipment.armor.DivingHelmetItem;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.render.fog.LavaFogModifier;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class DivingLavaFogModifier extends LavaFogModifier {
    @Override
    public void applyStartEndModifier(
        FogData data,
        Entity cameraEntity,
        BlockPos cameraPos,
        ClientWorld world,
        float viewDistance,
        RenderTickCounter tickCounter
    ) {
        super.applyStartEndModifier(data, cameraEntity, cameraPos, world, viewDistance, tickCounter);
        if (cameraEntity.isSpectator()) {
            return;
        }
        ItemStack divingHelmet = DivingHelmetItem.getWornItem(cameraEntity);
        if (divingHelmet.isEmpty() || divingHelmet.takesDamageFrom(world.getDamageSources().lava())) {
            return;
        }
        data.environmentalStart = -4.0f;
        data.environmentalEnd = 20.0f;
    }
}
