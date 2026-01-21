package com.zurrtum.create.infrastructure.items;

import com.google.common.collect.MapMaker;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public interface ItemInventoryProvider<T extends SmartBlockEntity> extends WorldlyContainerHolder {
    Map<Container, WorldlyContainer> WRAPPERS = new MapMaker().weakValues().makeMap();

    @Override
    @Nullable
    default WorldlyContainer getContainer(BlockState state, LevelAccessor world, BlockPos pos) {
        Container inventory = getInventory(state, world, pos, null, null);
        if (inventory == null) {
            return null;
        }
        if (inventory instanceof WorldlyContainer sidedInventory) {
            return sidedInventory;
        }
        return WRAPPERS.computeIfAbsent(inventory, SidedInventoryWrapper::new);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    default Container getInventory(
        @Nullable BlockState state,
        LevelAccessor world,
        BlockPos pos,
        @Nullable BlockEntity blockEntity,
        @Nullable Direction context
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
        return getInventory(world, pos, state, (T) blockEntity, context);
    }

    Class<T> getBlockEntityClass();

    @Nullable Container getInventory(LevelAccessor world, BlockPos pos, BlockState state, T blockEntity, @Nullable Direction context);
}
