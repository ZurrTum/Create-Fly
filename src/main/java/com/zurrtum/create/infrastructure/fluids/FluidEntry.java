package com.zurrtum.create.infrastructure.fluids;

import net.minecraft.world.item.BucketItem;
import org.jspecify.annotations.Nullable;

public class FluidEntry {
    public FlowableFluid flowing = null;
    public FlowableFluid still = null;
    public @Nullable BucketItem bucket = null;
    public @Nullable FluidBlock block = null;
}
