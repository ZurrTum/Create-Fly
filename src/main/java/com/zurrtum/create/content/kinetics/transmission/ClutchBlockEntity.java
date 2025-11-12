package com.zurrtum.create.content.kinetics.transmission;

import com.zurrtum.create.AllBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ClutchBlockEntity extends SplitShaftBlockEntity {

    public ClutchBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.CLUTCH, pos, state);
    }

    @Override
    public float getRotationSpeedModifier(Direction face) {
        if (hasSource()) {
            if (face != getSourceFacing() && getBlockState().getValue(BlockStateProperties.POWERED))
                return 0;
        }
        return 1;
    }

}
