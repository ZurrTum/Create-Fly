package com.zurrtum.create.client.infrastructure.fluid;

import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.content.equipment.armor.DivingHelmetItem;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.render.fog.WaterFogModifier;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.math.BlockPos;

public class FluidFogModifier extends WaterFogModifier {
    @Override
    public void applyStartEndModifier(
        FogData data,
        Entity cameraEntity,
        BlockPos cameraPos,
        ClientWorld world,
        float viewDistance,
        RenderTickCounter tickCounter
    ) {
        FluidConfig config = AllFluidConfigs.ALL.get(world.getFluidState(cameraPos).getFluid());
        if (config != null) {
            data.environmentalStart = -8.0F;
            data.environmentalEnd = config.fogDistance().get();
            if (cameraEntity instanceof ClientPlayerEntity clientPlayerEntity) {
                data.environmentalEnd = data.environmentalEnd * Math.max(0.25F, clientPlayerEntity.getUnderwaterVisibility());
                if (world.getBiome(cameraPos).isIn(BiomeTags.HAS_CLOSER_WATER_FOG)) {
                    data.environmentalEnd *= 0.85F;
                }
            }

            data.skyEnd = data.environmentalEnd;
            data.cloudEnd = data.environmentalEnd;
        } else {
            super.applyStartEndModifier(data, cameraEntity, cameraPos, world, viewDistance, tickCounter);
        }
        ItemStack divingHelmet = DivingHelmetItem.getWornItem(cameraEntity);
        if (!divingHelmet.isEmpty()) {
            data.environmentalEnd *= 6.25F;
        }
    }

    @Override
    public int getFogColor(ClientWorld world, Camera camera, int viewDistance, float skyDarkness) {
        FluidConfig config = AllFluidConfigs.ALL.get(world.getFluidState(camera.getBlockPos()).getFluid());
        if (config != null) {
            if (config.fogColor() != -1) {
                return config.fogColor();
            }
        }
        return super.getFogColor(world, camera, viewDistance, skyDarkness);
    }
}
