package com.zurrtum.create.infrastructure.fluids;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface FluidInventoryProvider<T extends SmartBlockEntity> {
    default FluidInventory getFluidInventory(BlockState state, LevelAccessor world, BlockPos pos) {
        return getFluidInventory(state, world, pos, null, null);
    }

    @SuppressWarnings("unchecked")
    default FluidInventory getFluidInventory(
        @Nullable BlockState state,
        LevelAccessor world,
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
                state = blockEntity.getBlockState();
            }
        }
        Class<T> expectedClass = getBlockEntityClass();
        if (!expectedClass.isInstance(blockEntity))
            return null;
        return getFluidInventory(world, pos, state, (T) blockEntity, context);
    }

    Class<T> getBlockEntityClass();

    FluidInventory getFluidInventory(LevelAccessor world, BlockPos pos, BlockState state, T blockEntity, Direction context);
}
