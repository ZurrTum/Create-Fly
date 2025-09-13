package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.zurrtum.create.content.redstone.smartObserver.SmartObserverBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.TankManipulationBehaviour;
import com.zurrtum.create.foundation.utility.FluidFormatter;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class FluidAmountDisplaySource extends SingleLineDisplaySource {
    @Override
    protected MutableText provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        BlockEntity sourceBE = context.getSourceBlockEntity();
        if (!(sourceBE instanceof SmartObserverBlockEntity cobe))
            return EMPTY_LINE;

        TankManipulationBehaviour tankManipulationBehaviour = cobe.getBehaviour(TankManipulationBehaviour.OBSERVE);
        ServerFilteringBehaviour filteringBehaviour = cobe.getBehaviour(ServerFilteringBehaviour.TYPE);
        FluidInventory handler = tankManipulationBehaviour.getInventory();

        if (handler == null)
            return EMPTY_LINE;

        long collected = 0;
        for (int i = 0, size = handler.size(); i < size; i++) {
            FluidStack stack = handler.getStack(i);
            if (stack.isEmpty())
                continue;
            if (!filteringBehaviour.test(stack))
                continue;
            collected += stack.getAmount();
        }

        return Text.literal(FluidFormatter.asString(collected, false));
    }

    @Override
    protected String getTranslationKey() {
        return "fluid_amount";
    }

    @Override
    public boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}
