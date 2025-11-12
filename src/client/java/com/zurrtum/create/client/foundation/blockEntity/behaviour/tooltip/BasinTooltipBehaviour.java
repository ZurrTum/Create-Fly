package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.catnip.lang.LangBuilder;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.content.processing.basin.BasinInventory;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class BasinTooltipBehaviour extends TooltipBehaviour<BasinBlockEntity> implements IHaveGoggleInformation {
    public BasinTooltipBehaviour(BasinBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("gui.goggles.basin_contents").forGoggles(tooltip);

        boolean isEmpty = true;

        BasinInventory itemCapability = blockEntity.itemCapability;
        if (itemCapability != null) {
            for (int i = 0, size = itemCapability.getContainerSize(); i < size; i++) {
                ItemStack stackInSlot = itemCapability.getItem(i);
                if (stackInSlot.isEmpty())
                    continue;
                CreateLang.text("").add(stackInSlot.getItemName().copy().withStyle(ChatFormatting.GRAY))
                    .add(CreateLang.text(" x" + stackInSlot.getCount()).style(ChatFormatting.GREEN)).forGoggles(tooltip, 1);
                isEmpty = false;
            }
        }

        FluidInventory fluidCapability = blockEntity.fluidCapability;
        if (fluidCapability != null) {
            LangBuilder mb = CreateLang.translate("generic.unit.millibuckets");
            for (int i = 0, size = fluidCapability.size(); i < size; i++) {
                FluidStack fluidStack = fluidCapability.getStack(i);
                if (fluidStack.isEmpty())
                    continue;
                CreateLang.text("").add(CreateLang.fluidName(fluidStack).add(CreateLang.text(" ")).style(ChatFormatting.GRAY)
                    .add(CreateLang.number((double) fluidStack.getAmount() / 81).add(mb).style(ChatFormatting.BLUE))).forGoggles(tooltip, 1);
                isEmpty = false;
            }
        }

        if (isEmpty)
            tooltip.removeFirst();

        return true;
    }
}
