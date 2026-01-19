package com.zurrtum.create.content.logistics.crate;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllMountedStorageTypes;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class CreativeCrateMountedStorage extends MountedItemStorage {
    public static final MapCodec<CreativeCrateMountedStorage> CODEC = ItemStack.OPTIONAL_CODEC.xmap(
        CreativeCrateMountedStorage::new,
        storage -> storage.suppliedStack
    ).fieldOf("value");
    private final ItemStack suppliedStack;
    private final int max;

    protected CreativeCrateMountedStorage(ItemStack suppliedStack) {
        super(AllMountedStorageTypes.CREATIVE_CRATE);
        this.suppliedStack = suppliedStack;
        this.max = suppliedStack.getOrDefault(DataComponents.MAX_STACK_SIZE, 64);
    }

    @Override
    public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        // no need to do anything here, the supplied item can't change while mounted
    }

    @Override
    public int getContainerSize() {
        return 2; // 0 holds the supplied stack endlessly, 1 is always empty to accept
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot == 0)
            return suppliedStack;
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
    }

    @Override
    public void setChanged() {
        suppliedStack.setCount(max);
    }
}
