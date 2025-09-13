package com.zurrtum.create.content.redstone.diodes;

import com.zurrtum.create.AllBlockEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

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

        if (world.isClient)
            return;

        boolean shouldPower = !powered && (maxState.getValue() == 2 ? state == 0 : state <= 1);
        BlockState blockState = getCachedState();
        if (blockState.get(POWERING) != shouldPower)
            world.setBlockState(pos, blockState.with(POWERING, shouldPower));
    }

}
