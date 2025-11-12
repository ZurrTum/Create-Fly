package com.zurrtum.create.content.fluids.hosePulley;

import com.zurrtum.create.content.fluids.transfer.FluidDrainingBehaviour;
import com.zurrtum.create.content.fluids.transfer.FluidFillingBehaviour;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import com.zurrtum.create.infrastructure.fluids.SidedFluidInventory;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class HosePulleyFluidHandler implements SidedFluidInventory {
    private static final int HALF_BUCKET = BucketFluidInventory.CAPACITY / 2;
    private static final int CAPACITY = BucketFluidInventory.CAPACITY + HALF_BUCKET;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final Optional<Integer> MAX = Optional.of(CAPACITY);
    private static final int[] SLOTS = {0, 1};
    private static final int[] EMPTY_SLOTS = new int[0];
    private final FluidFillingBehaviour filler;
    private final FluidDrainingBehaviour drainer;
    private final Supplier<BlockPos> rootPosGetter;
    private final Supplier<Boolean> predicate;
    private final HosePulleyBlockEntity be;
    private FluidStack stack = FluidStack.EMPTY;
    private int previousAmount = 0;

    public HosePulleyFluidHandler(
        HosePulleyBlockEntity be,
        FluidFillingBehaviour filler,
        FluidDrainingBehaviour drainer,
        Supplier<BlockPos> rootPosGetter,
        Supplier<Boolean> predicate
    ) {
        this.be = be;
        this.filler = filler;
        this.drainer = drainer;
        this.rootPosGetter = rootPosGetter;
        this.predicate = predicate;
    }

    @Override
    public int[] getAvailableSlots(@Nullable Direction side) {
        if (HosePulleyBlock.hasPipeTowards(be.getLevel(), be.getBlockPos(), be.getBlockState(), side)) {
            return SLOTS;
        } else {
            return EMPTY_SLOTS;
        }
    }

    @Override
    public boolean canInsert(int slot, FluidStack stack, @Nullable Direction dir) {
        if (slot != 0 || !FluidHelper.hasBlockState(stack.getFluid())) {
            return false;
        }
        return this.stack.getAmount() < BucketFluidInventory.CAPACITY || predicate.get() && filler.tryDeposit(
            stack.getFluid(),
            rootPosGetter.get(),
            true
        );
    }

    @Override
    public boolean canExtract(int slot, FluidStack stack, Direction dir) {
        if (slot == 1) {
            return predicate.get();
        }
        return true;
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public FluidStack onExtract(FluidStack stack) {
        return removeMaxSize(stack, MAX);
    }

    @Override
    public int getMaxAmountPerStack() {
        return CAPACITY;
    }

    @Override
    public FluidStack getStack(int slot) {
        if (slot > 1) {
            return FluidStack.EMPTY;
        }
        if (slot == 0) {
            return stack;
        }
        int amount = stack.getAmount();
        if (amount <= HALF_BUCKET && drainer.pullNext(rootPosGetter.get(), true)) {
            FluidStack stack = drainer.getDrainableFluid(rootPosGetter.get());
            if (!stack.isEmpty() && (amount == 0 || matches(this.stack, stack)) && drainer.pullNext(rootPosGetter.get(), false)) {
                filler.counterpartActed();
                setMaxSize(stack, MAX);
                if (amount > 0) {
                    stack.setAmount(amount + BucketFluidInventory.CAPACITY);
                }
                return this.stack = stack;
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public void setStack(int slot, FluidStack stack) {
        if (slot != 0) {
            return;
        }
        if (stack != FluidStack.EMPTY) {
            setMaxSize(stack, MAX);
        }
        this.stack = stack;
    }

    @Override
    public void markDirty() {
        int amount = stack.getAmount();
        if (amount > previousAmount) {
            if (amount >= BucketFluidInventory.CAPACITY && predicate.get() && filler.tryDeposit(stack.getFluid(), rootPosGetter.get(), false)) {
                drainer.counterpartActed();
                amount -= BucketFluidInventory.CAPACITY;
                if (amount == 0) {
                    stack = FluidStack.EMPTY;
                } else {
                    stack.setAmount(amount);
                }
            }
            be.onTankContentsChanged(stack);
        } else if (amount < previousAmount) {
            be.onTankContentsChanged(stack);
        }
        previousAmount = stack.getAmount();
    }

    public void write(ValueOutput view) {
        if (!stack.isEmpty()) {
            view.store("Fluid", FluidStack.CODEC, stack);
        }
    }

    public void read(ValueInput view) {
        stack = view.read("Fluid", FluidStack.CODEC).orElse(FluidStack.EMPTY);
    }
}
