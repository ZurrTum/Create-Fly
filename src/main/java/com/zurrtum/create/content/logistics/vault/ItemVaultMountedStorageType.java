package com.zurrtum.create.content.logistics.vault;

import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ItemVaultMountedStorageType extends MountedItemStorageType<ItemVaultMountedStorage> {
    public ItemVaultMountedStorageType() {
        super(ItemVaultMountedStorage.CODEC);
    }

    @Override
    @Nullable
    public ItemVaultMountedStorage mount(World level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        return be instanceof ItemVaultBlockEntity vault ? ItemVaultMountedStorage.fromVault(vault) : null;
    }
}
