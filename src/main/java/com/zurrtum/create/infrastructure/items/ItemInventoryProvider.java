package com.zurrtum.create.infrastructure.items;

import com.google.common.collect.MapMaker;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface ItemInventoryProvider<T extends SmartBlockEntity> extends InventoryProvider {
    Map<Inventory, SidedInventory> WRAPPERS = new MapMaker().weakValues().makeMap();

    @Override
    default SidedInventory getInventory(BlockState state, WorldAccess world, BlockPos pos) {
        Inventory inventory = getInventory(state, world, pos, null, null);
        if (inventory == null) {
            return null;
        }
        if (inventory instanceof SidedInventory sidedInventory) {
            return sidedInventory;
        }
        return WRAPPERS.computeIfAbsent(inventory, SidedInventoryWrapper::new);
    }

    @SuppressWarnings("unchecked")
    default Inventory getInventory(
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
        return getInventory(world, pos, state, (T) blockEntity, context);
    }

    Class<T> getBlockEntityClass();

    Inventory getInventory(WorldAccess world, BlockPos pos, BlockState state, T blockEntity, Direction context);
}
