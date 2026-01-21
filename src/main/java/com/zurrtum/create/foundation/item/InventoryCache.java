package com.zurrtum.create.foundation.item;

import com.zurrtum.create.AllTransfer;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

public class InventoryCache implements Supplier<@Nullable Container> {
    private final @Nullable BiPredicate<BlockEntity, Direction> filter;
    public final ServerLevel world;
    public final Direction direction;
    public final BlockPos pos;
    public boolean cached;
    public @Nullable Container inventory;
    public @Nullable Supplier<@Nullable Container> getter = this::refresh;

    public InventoryCache(ServerLevel world, BlockPos pos, Direction direction, @Nullable BiPredicate<BlockEntity, Direction> filter) {
        this.world = world;
        this.direction = direction;
        this.pos = pos;
        this.filter = filter;
    }

    @Override
    @Nullable
    public Container get() {
        if (cached) {
            return inventory;
        }
        return inventory = getter.get();
    }

    public void invalidate() {
        cached = false;
        getter = this::refresh;
    }

    @Nullable
    private Container refresh() {
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
        if (block instanceof WorldlyContainerHolder provider) {
            return provider.getContainer(state, world, pos);
        }
        if (blockEntity instanceof Container container) {
            if (blockEntity instanceof ChestBlockEntity && block instanceof ChestBlock chestBlock) {
                return ChestBlock.getContainer(chestBlock, state, world, pos, true);
            }
            return container;
        }
        getter = AllTransfer.getCacheInventory(world, pos, direction, filter);
        if (getter == null) {
            return null;
        }
        cached = false;
        return getter.get();
    }
}
