package com.zurrtum.create.content.contraptions.actors.psi;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.infrastructure.items.CombinedInvWrapper;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import com.zurrtum.create.infrastructure.items.SidedItemInventory;
import com.zurrtum.create.infrastructure.transfer.SlotRangeCache;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PortableItemInterfaceBlockEntity extends PortableStorageInterfaceBlockEntity {

    public final Inventory capability;

    public PortableItemInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.PORTABLE_STORAGE_INTERFACE, pos, state);
        capability = new InterfaceItemHandler();
    }

    @Override
    public void startTransferringTo(Contraption contraption, float distance) {
        ((InterfaceItemHandler) capability).setInventory(contraption.getStorage().getAllItems());
        super.startTransferringTo(contraption, distance);
    }

    @Override
    protected void stopTransferring() {
        ((InterfaceItemHandler) capability).setEmpty();
        super.stopTransferring();
    }

    class InterfaceItemHandler implements SidedItemInventory {
        private static final Inventory EMPTY = new ItemStackHandler(0);

        private int[] slots = SlotRangeCache.EMPTY;
        private Inventory wrapped = EMPTY;

        @Override
        public int[] getAvailableSlots(Direction side) {
            return slots;
        }

        @Override
        public boolean canExtract(int slot, ItemStack stack, Direction dir) {
            if (wrapped == EMPTY) {
                return false;
            }
            return ((SidedInventory) wrapped).canExtract(slot, stack, dir);
        }

        @Override
        public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
            if (wrapped == EMPTY) {
                return false;
            }
            return ((SidedInventory) wrapped).canInsert(slot, stack, dir);
        }

        public void setInventory(CombinedInvWrapper wrapped) {
            this.wrapped = wrapped;
            slots = wrapped.getAvailableSlots(null);
        }

        public void setEmpty() {
            wrapped = EMPTY;
            slots = SlotRangeCache.EMPTY;
        }

        @Override
        public int size() {
            return slots.length;
        }

        @Override
        public int getMaxCountPerStack() {
            return wrapped.getMaxCountPerStack();
        }

        @Override
        public int getMaxCount(ItemStack stack) {
            return wrapped.getMaxCount(stack);
        }

        @Override
        public ItemStack getStack(int slot) {
            return wrapped.getStack(slot);
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            wrapped.setStack(slot, stack);
        }

        @Override
        public int insert(ItemStack stack, int maxAmount, Direction side) {
            int insert = wrapped.insert(stack, maxAmount, side);
            if (insert != 0) {
                markDirty();
            }
            return insert;
        }

        @Override
        public int extract(ItemStack stack, int maxAmount, Direction side) {
            int extract = wrapped.extract(stack, maxAmount, side);
            if (extract != 0) {
                markDirty();
            }
            return extract;
        }

        @Override
        public void markDirty() {
            onContentTransferred();
        }

        @Override
        public @NotNull java.util.Iterator<ItemStack> iterator(Direction side) {
            return wrapped.iterator(side);
        }
    }

}
