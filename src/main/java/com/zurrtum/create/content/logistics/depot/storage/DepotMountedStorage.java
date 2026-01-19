package com.zurrtum.create.content.logistics.depot.storage;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllMountedStorageTypes;
import com.zurrtum.create.api.contraption.storage.SyncedMountedStorage;
import com.zurrtum.create.api.contraption.storage.item.WrapperMountedItemStorage;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.content.logistics.depot.DepotBlockEntity;
import com.zurrtum.create.infrastructure.items.ItemInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.jspecify.annotations.Nullable;

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
    public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof DepotBlockEntity depot) {
            wrapped.getHeld().ifPresentOrElse(depot::setHeldItem, depot::removeHeldItem);
        }
    }

    @Override
    public boolean handleInteraction(ServerPlayer player, Contraption contraption, StructureBlockInfo info) {
        // interaction is handled in the Interaction Behavior, swaps items with the player
        return false;
    }

    @Override
    public void setChanged() {
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
        BlockEntity be = AllClientHandle.INSTANCE.getBlockEntityClientSide(contraption, localPos);
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
        public int getContainerSize() {
            return 1;
        }

        @Override
        public ItemStack getItem(int slot) {
            if (slot > 1) {
                return ItemStack.EMPTY;
            }
            return held.map(Handler::getHeldStack).orElse(ItemStack.EMPTY);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
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
        public void setChanged() {
            dirty = true;
        }
    }
}
