package com.zurrtum.create.content.kinetics.base;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class DirectionalShaftHalvesBlockEntity extends KineticBlockEntity {

    public DirectionalShaftHalvesBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Direction getSourceFacing() {
        BlockPos localSource = source.subtract(getPos());
        return Direction.getFacing(localSource.getX(), localSource.getY(), localSource.getZ());
    }

}
