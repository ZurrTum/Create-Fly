package com.zurrtum.create.content.redstone.diodes;

import com.zurrtum.create.AllBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

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
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERING, true));
            state = maxState.getValue();
            return;
        }

        if (state == 1 && powering && !level.isClientSide()) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERING, false));
        }

        state--;
    }
}
