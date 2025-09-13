package com.zurrtum.create.content.kinetics.waterwheel;

import com.zurrtum.create.AllBlockEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class LargeWaterWheelBlockEntity extends WaterWheelBlockEntity {

    public LargeWaterWheelBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.LARGE_WATER_WHEEL, pos, state);
    }

    @Override
    protected int getSize() {
        return 2;
    }

}
