package com.zurrtum.create.infrastructure.fluids;

import com.zurrtum.create.AllFluids;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;

public class BucketFluidInventory extends FluidItemInventoryWrapper {
    public static final int CAPACITY = 81000;

    @Override
    public int getMaxAmountPerStack() {
        return CAPACITY;
    }

    public ItemStack toFillBucket(FluidStack stack) {
        if (stack.isOf(AllFluids.MILK)) {
            return Items.MILK_BUCKET.getDefaultInstance();
        }
        return stack.getFluid().getBucket().getDefaultInstance();
    }

    public Fluid toFluid() {
        if (stack.is(Items.MILK_BUCKET)) {
            return AllFluids.MILK;
        }
        return ((BucketItem) this.stack.getItem()).content;
    }

    @Override
    public boolean canInsert() {
        Item item = stack.getItem();
        return item == Items.BUCKET || item == Items.AIR;
    }

    @Override
    public boolean canExtract() {
        Item item = stack.getItem();
        return item != Items.BUCKET && item != Items.AIR;
    }

    @Override
    public int insert(FluidStack stack) {
        return insert(stack, stack.getAmount());
    }

    @Override
    public int insert(FluidStack stack, int maxAmount) {
        if (!canInsert() || maxAmount < CAPACITY) {
            return 0;
        }
        ItemStack bucket = toFillBucket(stack);
        if (bucket.isEmpty()) {
            return 0;
        }
        this.stack = bucket;
        return CAPACITY;
    }

    @Override
    public boolean preciseInsert(FluidStack stack) {
        return insert(stack) == CAPACITY;
    }

    @Override
    public int count(FluidStack stack) {
        Item item = this.stack.getItem();
        if (item == Items.BUCKET || item == Items.AIR || stack.getFluid() != toFluid()) {
            return 0;
        }
        return CAPACITY;
    }

    @Override
    public int count(FluidStack stack, int maxAmount) {
        if (maxAmount < CAPACITY) {
            return 0;
        }
        return count(stack);
    }

    @Override
    public int countSpace(FluidStack stack) {
        ItemStack bucket = toFillBucket(stack);
        if (bucket.isEmpty()) {
            return 0;
        }
        return canInsert() ? CAPACITY : 0;
    }

    @Override
    public int countSpace(FluidStack stack, int maxAmount) {
        if (maxAmount < CAPACITY) {
            return 0;
        }
        return countSpace(stack);
    }

    @Override
    public int extract(FluidStack stack) {
        return extract(stack, stack.getAmount());
    }

    @Override
    public int extract(FluidStack stack, int maxAmount) {
        if (!canExtract() || maxAmount != CAPACITY || stack.getFluid() != toFluid()) {
            return 0;
        }
        this.stack = Items.BUCKET.getDefaultInstance();
        return CAPACITY;
    }

    @Override
    public FluidStack extractAny(int maxAmount) {
        if (!canExtract() || maxAmount != CAPACITY) {
            return FluidStack.EMPTY;
        }
        FluidStack fluid = new FluidStack(toFluid(), CAPACITY);
        stack = Items.BUCKET.getDefaultInstance();
        return fluid;
    }

    @Override
    public boolean preciseExtract(FluidStack stack) {
        return extract(stack) == CAPACITY;
    }

    @Override
    public FluidStack getStack() {
        Item item = stack.getItem();
        if (item == Items.BUCKET || item == Items.AIR) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(toFluid(), CAPACITY);
    }

    @Override
    public void setStack(FluidStack stack) {
        if (stack.getAmount() >= CAPACITY) {
            this.stack = toFillBucket(stack);
        } else {
            this.stack = Items.BUCKET.getDefaultInstance();
        }
    }

    @Override
    public FluidStack removeStack() {
        Item item = stack.getItem();
        if (item == Items.BUCKET || item == Items.AIR) {
            return FluidStack.EMPTY;
        }
        stack = Items.BUCKET.getDefaultInstance();
        return new FluidStack(toFluid(), CAPACITY);
    }

    @Override
    public FluidStack removeStackWithAmount(int amount) {
        if (amount != CAPACITY) {
            return FluidStack.EMPTY;
        }
        return removeStack();
    }
}
