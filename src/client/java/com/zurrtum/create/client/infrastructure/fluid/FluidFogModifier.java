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
import org.joml.Vector3fc;

public class FluidFogModifier extends WaterFogEnvironment {
    @Override
    public void setupFog(FogData data, Camera camera, ClientLevel world, float viewDistance, DeltaTracker tickCounter) {
        Entity cameraEntity = camera.entity();
        BlockPos cameraPos = camera.blockPosition();
        FogDistanceSupplier supplier = AllFluidConfigs.FOG_DISTANCE.get(world.getFluidState(cameraPos).getType());
        if (supplier != null) {
            float partialTicks = tickCounter.getGameTimeDeltaPartialTick(false);
            data.environmentalStart = camera.attributeProbe()
                .getValue(EnvironmentAttributes.WATER_FOG_START_DISTANCE, partialTicks);
            data.environmentalEnd = supplier.get();
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
    public Vector3fc getBaseColor(ClientLevel world, Camera camera, int viewDistance, float skyDarkness) {
        Vector3fc color = AllFluidConfigs.FOG_COLOR.get(world.getFluidState(camera.blockPosition()).getType());
        if (color != null) {
            return color;
        }
        return super.getBaseColor(world, camera, viewDistance, skyDarkness);
    }
}
