package com.zurrtum.create.content.logistics.vault;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllMountedStorageTypes;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import com.zurrtum.create.api.contraption.storage.item.WrapperMountedItemStorage;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
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
    public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof ItemVaultBlockEntity vault) {
            vault.applyInventoryToBlock(this.wrapped);
        }
    }

    @Override
    public boolean handleInteraction(ServerPlayer player, Contraption contraption, StructureBlockInfo info) {
        // vaults should never be opened.
        return false;
    }

    public static ItemVaultMountedStorage fromVault(ItemVaultBlockEntity vault) {
        // Vault inventories have a world-affecting onContentsChanged, copy to a safe one
        return new ItemVaultMountedStorage(copyToItemStackHandler(vault.getInventoryOfBlock()));
    }
}
