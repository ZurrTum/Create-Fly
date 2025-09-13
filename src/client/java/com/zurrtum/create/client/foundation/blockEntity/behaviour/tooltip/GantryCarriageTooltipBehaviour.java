package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.content.contraptions.IDisplayAssemblyExceptions;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.gantry.GantryCarriageBlockEntity;

public class GantryCarriageTooltipBehaviour extends TooltipBehaviour<GantryCarriageBlockEntity> implements IDisplayAssemblyExceptions {
    public GantryCarriageTooltipBehaviour(GantryCarriageBlockEntity be) {
        super(be);
    }

    @Override
    public AssemblyException getLastAssemblyException() {
        return blockEntity.getLastAssemblyException();
    }
}
