package com.zurrtum.create.content.redstone.diodes;

import com.zurrtum.create.AllBlockEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import static com.zurrtum.create.content.redstone.diodes.BrassDiodeBlock.POWERING;

public class PulseExtenderBlockEntity extends BrassDiodeBlockEntity {

    public PulseExtenderBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.PULSE_EXTENDER, pos, state);
    }

    @Override
    protected void updateState(boolean powered, boolean powering, boolean atMax, boolean atMin) {
        if (atMin && !powered)
            return;
        if (atMin || powered) {
            world.setBlockState(pos, getCachedState().with(POWERING, true));
            state = maxState.getValue();
            return;
        }

        if (state == 1 && powering && !world.isClient()) {
            world.setBlockState(pos, getCachedState().with(POWERING, false));
        }

        state--;
    }
}
