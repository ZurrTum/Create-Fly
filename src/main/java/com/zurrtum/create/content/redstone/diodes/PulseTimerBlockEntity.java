package com.zurrtum.create.content.redstone.diodes;

import com.zurrtum.create.AllBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import static com.zurrtum.create.content.redstone.diodes.BrassDiodeBlock.POWERING;

public class PulseTimerBlockEntity extends BrassDiodeBlockEntity {

    public PulseTimerBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.PULSE_TIMER, pos, state);
    }

    @Override
    protected int defaultValue() {
        return 20;
    }

    @Override
    protected void updateState(boolean powered, boolean powering, boolean atMax, boolean atMin) {
        if (powered || state >= maxState.getValue() - 1)
            state = 0;
        else
            state++;

        if (level.isClientSide())
            return;

        boolean shouldPower = !powered && (maxState.getValue() == 2 ? state == 0 : state <= 1);
        BlockState blockState = getBlockState();
        if (blockState.getValue(POWERING) != shouldPower)
            level.setBlockAndUpdate(worldPosition, blockState.setValue(POWERING, shouldPower));
    }

}
