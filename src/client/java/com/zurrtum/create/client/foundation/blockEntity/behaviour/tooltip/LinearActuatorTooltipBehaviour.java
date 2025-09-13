package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.content.contraptions.IDisplayAssemblyExceptions;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.piston.LinearActuatorBlockEntity;

public class LinearActuatorTooltipBehaviour extends KineticTooltipBehaviour<LinearActuatorBlockEntity> implements IDisplayAssemblyExceptions {
    public LinearActuatorTooltipBehaviour(LinearActuatorBlockEntity be) {
        super(be);
    }

    @Override
    public AssemblyException getLastAssemblyException() {
        return blockEntity.getLastAssemblyException();
    }
}
