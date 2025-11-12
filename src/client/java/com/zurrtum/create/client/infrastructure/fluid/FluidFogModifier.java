package com.zurrtum.create.client.infrastructure.fluid;

import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.content.equipment.armor.DivingHelmetItem;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.WaterFogEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class FluidFogModifier extends WaterFogEnvironment {
    @Override
    public void setupFog(FogData data, Entity cameraEntity, BlockPos cameraPos, ClientLevel world, float viewDistance, DeltaTracker tickCounter) {
        FluidConfig config = AllFluidConfigs.ALL.get(world.getFluidState(cameraPos).getType());
        if (config != null) {
            data.environmentalStart = -8.0F;
            data.environmentalEnd = config.fogDistance().get();
            if (cameraEntity instanceof LocalPlayer clientPlayerEntity) {
                data.environmentalEnd = data.environmentalEnd * Math.max(0.25F, clientPlayerEntity.getWaterVision());
                if (world.getBiome(cameraPos).is(BiomeTags.HAS_CLOSER_WATER_FOG)) {
                    data.environmentalEnd *= 0.85F;
                }
            }

            data.skyEnd = data.environmentalEnd;
            data.cloudEnd = data.environmentalEnd;
        } else {
            super.setupFog(data, cameraEntity, cameraPos, world, viewDistance, tickCounter);
        }
        ItemStack divingHelmet = DivingHelmetItem.getWornItem(cameraEntity);
        if (!divingHelmet.isEmpty()) {
            data.environmentalEnd *= 6.25F;
        }
    }

    @Override
    public int getBaseColor(ClientLevel world, Camera camera, int viewDistance, float skyDarkness) {
        FluidConfig config = AllFluidConfigs.ALL.get(world.getFluidState(camera.getBlockPosition()).getType());
        if (config != null) {
            if (config.fogColor() != -1) {
                return config.fogColor();
            }
        }
        return super.getBaseColor(world, camera, viewDistance, skyDarkness);
    }
}
