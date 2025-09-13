package com.zurrtum.create.content.logistics.vault;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllMountedStorageTypes;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import com.zurrtum.create.api.contraption.storage.item.WrapperMountedItemStorage;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ItemVaultMountedStorage extends WrapperMountedItemStorage<ItemStackHandler> {
    public static final MapCodec<ItemVaultMountedStorage> CODEC = CreateCodecs.ITEM_STACK_HANDLER.xmap(
        ItemVaultMountedStorage::new,
        storage -> storage.wrapped
    ).fieldOf("value");

    protected ItemVaultMountedStorage(MountedItemStorageType<?> type, ItemStackHandler handler) {
        super(type, handler);
    }

    protected ItemVaultMountedStorage(ItemStackHandler handler) {
        this(AllMountedStorageTypes.VAULT, handler);
    }

    @Override
    public void unmount(World level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof ItemVaultBlockEntity vault) {
            vault.applyInventoryToBlock(this.wrapped);
        }
    }

    @Override
    public boolean handleInteraction(ServerPlayerEntity player, Contraption contraption, StructureBlockInfo info) {
        // vaults should never be opened.
        return false;
    }

    public static ItemVaultMountedStorage fromVault(ItemVaultBlockEntity vault) {
        // Vault inventories have a world-affecting onContentsChanged, copy to a safe one
        return new ItemVaultMountedStorage(copyToItemStackHandler(vault.getInventoryOfBlock()));
    }
}
