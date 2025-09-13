package com.zurrtum.create.content.kinetics.transmission;

import com.zurrtum.create.AllBlockEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class GearshiftBlockEntity extends SplitShaftBlockEntity {

    public GearshiftBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.GEARSHIFT, pos, state);
    }

    @Override
    public float getRotationSpeedModifier(Direction face) {
        if (hasSource()) {
            if (face != getSourceFacing() && getCachedState().get(Properties.POWERED))
                return -1;
        }
        return 1;
    }

}
