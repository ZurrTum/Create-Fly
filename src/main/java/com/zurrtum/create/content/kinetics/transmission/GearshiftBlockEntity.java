package com.zurrtum.create.content.kinetics.transmission;

import com.zurrtum.create.AllBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class GearshiftBlockEntity extends SplitShaftBlockEntity {

    public GearshiftBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.GEARSHIFT, pos, state);
    }

    @Override
    public float getRotationSpeedModifier(Direction face) {
        if (hasSource()) {
            if (face != getSourceFacing() && getBlockState().getValue(BlockStateProperties.POWERED))
                return -1;
        }
        return 1;
    }

}
