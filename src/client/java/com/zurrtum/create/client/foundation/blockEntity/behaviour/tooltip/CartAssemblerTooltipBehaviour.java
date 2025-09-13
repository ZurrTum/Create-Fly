package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.content.contraptions.IDisplayAssemblyExceptions;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.mounted.CartAssemblerBlockEntity;

public class CartAssemblerTooltipBehaviour extends TooltipBehaviour<CartAssemblerBlockEntity> implements IDisplayAssemblyExceptions {
    public CartAssemblerTooltipBehaviour(CartAssemblerBlockEntity be) {
        super(be);
    }

    @Override
    public AssemblyException getLastAssemblyException() {
        return blockEntity.getLastAssemblyException();
    }
}
