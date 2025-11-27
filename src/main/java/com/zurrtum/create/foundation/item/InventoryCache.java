package com.zurrtum.create.foundation.item;

import com.zurrtum.create.AllTransfer;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

public class InventoryCache implements Supplier<Inventory> {
    private final BiPredicate<BlockEntity, Direction> filter;
    public final ServerWorld world;
    public final Direction direction;
    public final BlockPos pos;
    public boolean cached;
    public Inventory inventory;
    public Supplier<Inventory> getter = this::refresh;

    public InventoryCache(ServerWorld world, BlockPos pos, Direction direction, BiPredicate<BlockEntity, Direction> filter) {
        this.world = world;
        this.direction = direction;
        this.pos = pos;
        this.filter = filter;
    }

    @Override
    public Inventory get() {
        if (cached) {
            return inventory;
        }
        return inventory = getter.get();
    }

    public void invalidate() {
        cached = false;
        getter = this::refresh;
    }

    private Inventory refresh() {
        cached = true;
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null && filter != null && !filter.test(blockEntity, direction)) {
            return null;
        }
        if (block instanceof ItemInventoryProvider<?> provider) {
            return provider.getInventory(state, world, pos, blockEntity, direction);
        }
        if (block instanceof InventoryProvider provider) {
            return provider.getInventory(state, world, pos);
        }
        if (blockEntity instanceof Inventory entityInventory) {
            if (blockEntity instanceof ChestBlockEntity && block instanceof ChestBlock chestBlock) {
                return ChestBlock.getInventory(chestBlock, state, world, pos, true);
            }
            return entityInventory;
        }
        getter = AllTransfer.getCacheInventory(world, pos, direction, filter);
        if (getter == null) {
            return null;
        }
        cached = false;
        return getter.get();
    }
}
