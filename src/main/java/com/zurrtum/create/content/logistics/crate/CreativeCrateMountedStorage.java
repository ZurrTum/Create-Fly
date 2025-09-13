package com.zurrtum.create.content.logistics.crate;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllMountedStorageTypes;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorage;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

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
        this.max = suppliedStack.getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 64);
    }

    @Override
    public void unmount(World level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        // no need to do anything here, the supplied item can't change while mounted
    }

    @Override
    public int size() {
        return 2; // 0 holds the supplied stack endlessly, 1 is always empty to accept
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot == 0)
            return suppliedStack;
        return ItemStack.EMPTY;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
    }

    @Override
    public void markDirty() {
        suppliedStack.setCount(max);
    }
}
