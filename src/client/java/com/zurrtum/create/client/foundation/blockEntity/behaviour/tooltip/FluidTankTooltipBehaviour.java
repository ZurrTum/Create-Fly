package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.stress.BlockStressValues;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.fluids.tank.BoilerData;
import com.zurrtum.create.content.fluids.tank.FluidTankBlockEntity;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;

public class FluidTankTooltipBehaviour extends TooltipBehaviour<FluidTankBlockEntity> implements IHaveGoggleInformation {
    public FluidTankTooltipBehaviour(FluidTankBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        FluidTankBlockEntity controllerBE = blockEntity.getControllerBE();
        if (controllerBE == null)
            return false;
        if (addBoilerDataTooltip(controllerBE.boiler, tooltip, controllerBE.getTotalTankSize()))
            return true;
        return containedFluidTooltip(
            tooltip,
            isPlayerSneaking,
            FluidHelper.getFluidInventory(blockEntity.getLevel(), controllerBE.getBlockPos(), null, blockEntity, null)
        );
    }

    public boolean addBoilerDataTooltip(BoilerData data, List<Component> tooltip, int boilerSize) {
        if (!data.isActive())
            return false;

        data.calcMinMaxForSize(boilerSize);

        CreateLang.translate("boiler.status", data.getHeatLevelTextComponent().withStyle(ChatFormatting.GREEN)).forGoggles(tooltip);
        CreateLang.builder().add(data.getSizeComponent(true, false)).forGoggles(tooltip, 1);
        CreateLang.builder().add(data.getWaterComponent(true, false)).forGoggles(tooltip, 1);
        CreateLang.builder().add(data.getHeatComponent(true, false)).forGoggles(tooltip, 1);

        if (data.attachedEngines == 0)
            return true;

        int boilerLevel = Math.min(data.activeHeat, Math.min(data.maxHeatForWater, data.maxHeatForSize));
        double totalSU = data.getEngineEfficiency(boilerSize) * 16 * Math.max(boilerLevel, data.attachedEngines) * BlockStressValues.getCapacity(
            AllBlocks.STEAM_ENGINE);

        tooltip.add(CommonComponents.EMPTY);

        if (data.attachedEngines > 0 && data.maxHeatForSize > 0 && data.maxHeatForWater == 0 && (data.passiveHeat ? 1 : data.activeHeat) > 0) {
            CreateLang.translate("boiler.water_input_rate").style(ChatFormatting.GRAY).forGoggles(tooltip);
            CreateLang.number(data.waterSupply / 81).style(ChatFormatting.BLUE).add(CreateLang.translate("generic.unit.millibuckets"))
                .add(CreateLang.text(" / ").style(ChatFormatting.GRAY)).add(CreateLang.translate(
                    "boiler.per_tick",
                    CreateLang.number((double) BoilerData.waterSupplyPerLevel / 81).add(CreateLang.translate("generic.unit.millibuckets"))
                ).style(ChatFormatting.DARK_GRAY)).forGoggles(tooltip, 1);
            return true;
        }

        CreateLang.translate("tooltip.capacityProvided").style(ChatFormatting.GRAY).forGoggles(tooltip);

        CreateLang.number(totalSU).translate("generic.unit.stress").style(ChatFormatting.AQUA).space()
            .add((data.attachedEngines == 1 ? CreateLang.translate("boiler.via_one_engine") : CreateLang.translate(
                "boiler.via_engines",
                data.attachedEngines
            )).style(ChatFormatting.DARK_GRAY)).forGoggles(tooltip, 1);

        return true;
    }
}
