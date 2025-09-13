package com.zurrtum.create.foundation.utility;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class FluidFormatter {
    public static String asString(long amount, boolean shorten) {
        Couple<MutableText> couple = asComponents(amount, shorten);
        return couple.getFirst().getString() + " " + couple.getSecond().getString();
    }

    public static Couple<MutableText> asComponents(long amount, boolean shorten) {
        if (shorten && amount >= BucketFluidInventory.CAPACITY) {
            return Couple.create(
                Text.literal(String.format("%.1f", (float) (amount / BucketFluidInventory.CAPACITY))),
                Text.translatable("create.generic.unit.buckets")
            );
        }

        return Couple.create(Text.literal(String.valueOf(amount)), Text.translatable("create.generic.unit.millibuckets"));
    }
}
