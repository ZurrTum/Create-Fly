package com.zurrtum.create.foundation.utility;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class FluidFormatter {
    public static String asString(long amount, boolean shorten) {
        Couple<MutableComponent> couple = asComponents(amount, shorten);
        return couple.getFirst().getString() + " " + couple.getSecond().getString();
    }

    public static Couple<MutableComponent> asComponents(long amount, boolean shorten) {
        if (shorten && amount >= BucketFluidInventory.CAPACITY) {
            return Couple.create(
                Component.literal(String.format("%.1f", (float) (amount / BucketFluidInventory.CAPACITY))),
                Component.translatable("create.generic.unit.buckets")
            );
        }

        return Couple.create(Component.literal(String.valueOf(amount)), Component.translatable("create.generic.unit.millibuckets"));
    }
}
