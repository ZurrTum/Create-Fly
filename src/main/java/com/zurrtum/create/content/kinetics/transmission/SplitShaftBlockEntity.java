package com.zurrtum.create.content.kinetics.transmission;

import com.zurrtum.create.content.kinetics.base.DirectionalShaftHalvesBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public abstract class SplitShaftBlockEntity extends DirectionalShaftHalvesBlockEntity {

    public SplitShaftBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract float getRotationSpeedModifier(Direction face);

}
