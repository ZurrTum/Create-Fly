package com.zurrtum.create.content.contraptions.actors.psi;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.infrastructure.items.CombinedInvWrapper;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import com.zurrtum.create.infrastructure.items.SidedItemInventory;
import com.zurrtum.create.infrastructure.transfer.SlotRangeCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PortableItemInterfaceBlockEntity extends PortableStorageInterfaceBlockEntity {

    public final Container capability;

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
        private static final Container EMPTY = new ItemStackHandler(0);

        private int[] slots = SlotRangeCache.EMPTY;
        private Container wrapped = EMPTY;
        private boolean mark = false;

        @Override
        public int[] getSlotsForFace(Direction side) {
            return slots;
        }

        @Override
        public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
            if (wrapped == EMPTY) {
                return false;
            }
            return ((WorldlyContainer) wrapped).canTakeItemThroughFace(slot, stack, dir);
        }

        @Override
        public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
            if (wrapped == EMPTY) {
                return false;
            }
            return ((WorldlyContainer) wrapped).canPlaceItemThroughFace(slot, stack, dir);
        }

        public void setInventory(CombinedInvWrapper wrapped) {
            this.wrapped = wrapped;
            slots = wrapped.getSlotsForFace(null);
        }

        public void setEmpty() {
            wrapped = EMPTY;
            slots = SlotRangeCache.EMPTY;
        }

        @Override
        public int getContainerSize() {
            return slots.length;
        }

        @Override
        public int getMaxStackSize() {
            return wrapped.getMaxStackSize();
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return wrapped.getMaxStackSize(stack);
        }

        @Override
        public ItemStack getItem(int slot) {
            mark = true;
            return wrapped.getItem(slot);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            mark = true;
            wrapped.setItem(slot, stack);
        }

        @Override
        public int insert(ItemStack stack, int maxAmount, Direction side) {
            int insert = wrapped.insert(stack, maxAmount, side);
            if (insert != 0) {
                setChanged();
            }
            return insert;
        }

        @Override
        public int extract(ItemStack stack, int maxAmount, Direction side) {
            int extract = wrapped.extract(stack, maxAmount, side);
            if (extract != 0) {
                setChanged();
            }
            return extract;
        }

        @Override
        public void setChanged() {
            onContentTransferred();
            if (mark) {
                mark = false;
                wrapped.setChanged();
            }
        }

        @Override
        public @NotNull java.util.Iterator<ItemStack> iterator(Direction side) {
            return wrapped.iterator(side);
        }
    }

}
