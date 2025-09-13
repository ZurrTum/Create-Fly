package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.catnip.lang.LangBuilder;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.content.processing.basin.BasinInventory;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class BasinTooltipBehaviour extends TooltipBehaviour<BasinBlockEntity> implements IHaveGoggleInformation {
    public BasinTooltipBehaviour(BasinBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToGoggleTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("gui.goggles.basin_contents").forGoggles(tooltip);

        boolean isEmpty = true;

        BasinInventory itemCapability = blockEntity.itemCapability;
        if (itemCapability != null) {
            for (int i = 0, size = itemCapability.size(); i < size; i++) {
                ItemStack stackInSlot = itemCapability.getStack(i);
                if (stackInSlot.isEmpty())
                    continue;
                CreateLang.text("").add(stackInSlot.getItemName().copy().formatted(Formatting.GRAY))
                    .add(CreateLang.text(" x" + stackInSlot.getCount()).style(Formatting.GREEN)).forGoggles(tooltip, 1);
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
                CreateLang.text("").add(CreateLang.fluidName(fluidStack).add(CreateLang.text(" ")).style(Formatting.GRAY)
                    .add(CreateLang.number((double) fluidStack.getAmount() / 81).add(mb).style(Formatting.BLUE))).forGoggles(tooltip, 1);
                isEmpty = false;
            }
        }

        if (isEmpty)
            tooltip.removeFirst();

        return true;
    }
}
