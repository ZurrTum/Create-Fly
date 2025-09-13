package com.zurrtum.create.content.logistics.depot.storage;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllMountedStorageTypes;
import com.zurrtum.create.api.contraption.storage.SyncedMountedStorage;
import com.zurrtum.create.api.contraption.storage.item.WrapperMountedItemStorage;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.content.logistics.depot.DepotBlockEntity;
import com.zurrtum.create.infrastructure.items.ItemInventory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class DepotMountedStorage extends WrapperMountedItemStorage<DepotMountedStorage.Handler> implements SyncedMountedStorage {
    public static final MapCodec<DepotMountedStorage> CODEC = TransportedItemStack.CODEC.optionalFieldOf("value")
        .xmap(DepotMountedStorage::new, DepotMountedStorage::getHeld);

    private boolean dirty;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected DepotMountedStorage(Optional<TransportedItemStack> stack) {
        super(AllMountedStorageTypes.DEPOT);
        wrapped = new Handler(stack);
    }

    @Override
    public void unmount(World level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof DepotBlockEntity depot) {
            wrapped.getHeld().ifPresentOrElse(depot::setHeldItem, depot::removeHeldItem);
        }
    }

    @Override
    public boolean handleInteraction(ServerPlayerEntity player, Contraption contraption, StructureBlockInfo info) {
        // interaction is handled in the Interaction Behavior, swaps items with the player
        return false;
    }

    @Override
    public void markDirty() {
        dirty = true;
    }

    @Override
    public boolean isDirty() {
        return this.dirty;
    }

    @Override
    public void markClean() {
        this.dirty = false;
    }

    @Override
    public void afterSync(Contraption contraption, BlockPos localPos) {
        BlockEntity be = contraption.presentBlockEntities.get(localPos);
        if (be instanceof DepotBlockEntity depot) {
            getHeld().ifPresentOrElse(depot::setHeldItem, depot::removeHeldItem);
        }
    }

    public void setHeld(TransportedItemStack stack) {
        wrapped.setHeld(Optional.of(stack));
    }

    public void removeHeldItem() {
        wrapped.setHeld(Optional.empty());
    }

    public Optional<TransportedItemStack> getHeld() {
        return wrapped.getHeld();
    }

    public static DepotMountedStorage fromDepot(DepotBlockEntity depot) {
        TransportedItemStack held = depot.getHeldItem();
        return new DepotMountedStorage(held != null ? Optional.of(held.copy()) : Optional.empty());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public class Handler implements ItemInventory {
        private Optional<TransportedItemStack> held;

        public Handler(Optional<TransportedItemStack> stack) {
            this.held = stack;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public ItemStack getStack(int slot) {
            if (slot > 1) {
                return ItemStack.EMPTY;
            }
            return held.map(Handler::getHeldStack).orElse(ItemStack.EMPTY);
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            if (slot > 1) {
                return;
            }
            if (stack.isEmpty()) {
                this.held = Optional.empty();
            } else {
                this.held = Optional.of(new TransportedItemStack(stack));
            }
        }

        private static ItemStack getHeldStack(TransportedItemStack held) {
            return held.stack;
        }

        public Optional<TransportedItemStack> getHeld() {
            return held;
        }

        public void setHeld(Optional<TransportedItemStack> stack) {
            this.held = stack;
        }

        @Override
        public void markDirty() {
            dirty = true;
        }
    }
}
