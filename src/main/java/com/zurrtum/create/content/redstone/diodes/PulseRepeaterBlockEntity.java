package com.zurrtum.create.content.redstone.diodes;

import com.zurrtum.create.AllBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import static com.zurrtum.create.content.redstone.diodes.BrassDiodeBlock.POWERING;

public class PulseRepeaterBlockEntity extends BrassDiodeBlockEntity {

    public PulseRepeaterBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.PULSE_REPEATER, pos, state);
    }

    @Override
    protected void updateState(boolean powered, boolean powering, boolean atMax, boolean atMin) {
        if (atMin && !powered)
            return;
        if (state > maxState.getValue() + 1) {
            if (!powered && !powering)
                state = 0;
            return;
        }

        state++;
        if (level.isClientSide())
            return;

        if (state == maxState.getValue() - 1 && !powering)
            level.setBlockAndUpdate(worldPosition, getBlockState().cycle(POWERING));
        if (state == maxState.getValue() + 1 && powering)
            level.setBlockAndUpdate(worldPosition, getBlockState().cycle(POWERING));
    }

}
