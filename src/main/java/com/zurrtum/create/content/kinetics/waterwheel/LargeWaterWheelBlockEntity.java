package com.zurrtum.create.content.kinetics.waterwheel;

import com.zurrtum.create.AllBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class LargeWaterWheelBlockEntity extends WaterWheelBlockEntity {

    public LargeWaterWheelBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.LARGE_WATER_WHEEL, pos, state);
    }

    @Override
    protected int getSize() {
        return 2;
    }

}
