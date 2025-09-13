package com.zurrtum.create.infrastructure.fluids;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public interface FluidInventoryProvider<T extends SmartBlockEntity> {
    default FluidInventory getFluidInventory(BlockState state, WorldAccess world, BlockPos pos) {
        return getFluidInventory(state, world, pos, null, null);
    }

    @SuppressWarnings("unchecked")
    default FluidInventory getFluidInventory(
        @Nullable BlockState state,
        WorldAccess world,
        BlockPos pos,
        @Nullable BlockEntity blockEntity,
        Direction context
    ) {
        if (blockEntity == null) {
            if (state == null) {
                state = world.getBlockState(pos);
            }
            if (state.hasBlockEntity()) {
                blockEntity = world.getBlockEntity(pos);
            }
            if (blockEntity == null) {
                return null;
            }
        } else {
            if (state == null) {
                state = blockEntity.getCachedState();
            }
        }
        Class<T> expectedClass = getBlockEntityClass();
        if (!expectedClass.isInstance(blockEntity))
            return null;
        return getFluidInventory(world, pos, state, (T) blockEntity, context);
    }

    Class<T> getBlockEntityClass();

    FluidInventory getFluidInventory(WorldAccess world, BlockPos pos, BlockState state, T blockEntity, Direction context);
}
