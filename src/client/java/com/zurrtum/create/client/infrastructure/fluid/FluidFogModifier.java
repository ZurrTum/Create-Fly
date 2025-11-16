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
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class FluidFogModifier extends WaterFogEnvironment {
    @Override
    public void setupFog(FogData data, Camera camera, ClientLevel world, float viewDistance, DeltaTracker tickCounter) {
        Entity cameraEntity = camera.entity();
        BlockPos cameraPos = camera.blockPosition();
        FluidConfig config = AllFluidConfigs.ALL.get(world.getFluidState(cameraPos).getType());
        if (config != null) {
            float partialTicks = tickCounter.getGameTimeDeltaPartialTick(false);
            data.environmentalStart = camera.attributeProbe().getValue(EnvironmentAttributes.WATER_FOG_START_DISTANCE, partialTicks);
            data.environmentalEnd = config.fogDistance().get();
            if (camera.entity() instanceof LocalPlayer player) {
                data.environmentalEnd = data.environmentalEnd * Math.max(0.25F, player.getWaterVision());
            }

            data.skyEnd = data.environmentalEnd;
            data.cloudEnd = data.environmentalEnd;
        } else {
            super.setupFog(data, camera, world, viewDistance, tickCounter);
        }
        ItemStack divingHelmet = DivingHelmetItem.getWornItem(cameraEntity);
        if (!divingHelmet.isEmpty()) {
            data.environmentalEnd *= 6.25F;
        }
    }

    @Override
    public int getBaseColor(ClientLevel world, Camera camera, int viewDistance, float skyDarkness) {
        FluidConfig config = AllFluidConfigs.ALL.get(world.getFluidState(camera.blockPosition()).getType());
        if (config != null) {
            if (config.fogColor() != -1) {
                return config.fogColor();
            }
        }
        return super.getBaseColor(world, camera, viewDistance, skyDarkness);
    }
}
