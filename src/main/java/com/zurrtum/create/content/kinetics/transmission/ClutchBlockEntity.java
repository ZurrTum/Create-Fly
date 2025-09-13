package com.zurrtum.create.content.kinetics.transmission;

import com.zurrtum.create.AllBlockEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ClutchBlockEntity extends SplitShaftBlockEntity {

    public ClutchBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.CLUTCH, pos, state);
    }

    @Override
    public float getRotationSpeedModifier(Direction face) {
        if (hasSource()) {
            if (face != getSourceFacing() && getCachedState().get(Properties.POWERED))
                return 0;
        }
        return 1;
    }

}
