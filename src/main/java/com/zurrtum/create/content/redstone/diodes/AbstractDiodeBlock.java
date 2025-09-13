package com.zurrtum.create.content.redstone.diodes;

import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BlockState;

public abstract class AbstractDiodeBlock extends AbstractRedstoneGateBlock implements IWrenchable {

    public AbstractDiodeBlock(Settings builder) {
        super(builder);
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }
}
