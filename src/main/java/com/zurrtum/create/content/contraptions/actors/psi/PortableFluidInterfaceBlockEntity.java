package com.zurrtum.create.content.contraptions.actors.psi;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.foundation.fluid.FluidTank;
import com.zurrtum.create.infrastructure.fluids.CombinedTankWrapper;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import com.zurrtum.create.infrastructure.fluids.SidedFluidInventory;
import com.zurrtum.create.infrastructure.transfer.SlotRangeCache;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PortableFluidInterfaceBlockEntity extends PortableStorageInterfaceBlockEntity {

    public final FluidInventory capability;

    public PortableFluidInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.PORTABLE_FLUID_INTERFACE, pos, state);
        capability = new InterfaceFluidHandler();
    }

    @Override
    public void startTransferringTo(Contraption contraption, float distance) {
        ((InterfaceFluidHandler) capability).setInventory(contraption.getStorage().getFluids());
        super.startTransferringTo(contraption, distance);
    }

    @Override
    protected void stopTransferring() {
        ((InterfaceFluidHandler) capability).setEmpty();
        super.stopTransferring();
    }

    public class InterfaceFluidHandler implements SidedFluidInventory {
        private static final FluidTank EMPTY = new FluidTank(0);

        private int[] slots = SlotRangeCache.EMPTY;
        private FluidInventory wrapped = EMPTY;

        @Override
        public int[] getAvailableSlots(Direction side) {
            return slots;
        }

        @Override
        public boolean canExtract(int slot, FluidStack stack, Direction dir) {
            if (wrapped == EMPTY) {
                return false;
            }
            return ((SidedFluidInventory) wrapped).canExtract(slot, stack, dir);
        }

        @Override
        public boolean canInsert(int slot, FluidStack stack, @Nullable Direction dir) {
            if (wrapped == EMPTY) {
                return false;
            }
            return ((SidedFluidInventory) wrapped).canInsert(slot, stack, dir);
        }

        public void setInventory(CombinedTankWrapper wrapped) {
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
        public int getMaxAmountPerStack() {
            return wrapped.getMaxAmountPerStack();
        }

        @Override
        public int getMaxAmount(FluidStack stack) {
            return wrapped.getMaxAmount(stack);
        }

        @Override
        public FluidStack getStack(int slot) {
            return wrapped.getStack(slot);
        }

        @Override
        public void setStack(int slot, FluidStack stack) {
            wrapped.setStack(slot, stack);
        }

        @Override
        public int insert(FluidStack stack, int maxAmount, Direction side) {
            int insert = wrapped.insert(stack, maxAmount, side);
            if (insert != 0) {
                markDirty();
            }
            return insert;
        }

        @Override
        public int extract(FluidStack stack, int maxAmount, Direction side) {
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
        public @NotNull java.util.Iterator<FluidStack> iterator(Direction side) {
            return wrapped.iterator(side);
        }
    }

}
