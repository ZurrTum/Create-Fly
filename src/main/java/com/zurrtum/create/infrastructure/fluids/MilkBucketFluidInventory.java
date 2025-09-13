package com.zurrtum.create.infrastructure.fluids;

import com.zurrtum.create.AllFluids;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class MilkBucketFluidInventory extends BucketFluidInventory {
    @Override
    public ItemStack toFillBucket(FluidStack stack) {
        return Items.MILK_BUCKET.getDefaultStack();
    }

    @Override
    public Fluid toFluid() {
        return AllFluids.MILK;
    }
}
