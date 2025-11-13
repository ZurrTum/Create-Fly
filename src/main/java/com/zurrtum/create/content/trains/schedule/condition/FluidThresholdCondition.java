package com.zurrtum.create.content.trains.schedule.condition;

import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class FluidThresholdCondition extends CargoThresholdCondition {
    public FilterItemStack compareStack = FilterItemStack.empty();

    public FluidThresholdCondition(Identifier id) {
        super(id);
    }

    @Override
    protected boolean test(World level, Train train, NbtCompound context) {
        Ops operator = getOperator();
        int target = getThreshold();

        int foundFluid = 0;
        for (Carriage carriage : train.carriages) {
            for (FluidStack fluidInTank : carriage.storage.getFluids()) {
                if (!compareStack.test(level, fluidInTank))
                    continue;
                foundFluid += fluidInTank.getAmount();
            }
        }

        requestStatusToUpdate(foundFluid / 81000, context);
        return operator.test(foundFluid, target * 81000);
    }

    @Override
    protected void writeAdditional(WriteView view) {
        view.put("Bucket", FilterItemStack.CODEC, compareStack);
    }

    @Override
    protected void readAdditional(ReadView view) {
        super.readAdditional(view);
        view.read("Bucket", FilterItemStack.CODEC).ifPresent(compareStack -> this.compareStack = compareStack);
    }

    @Override
    public MutableText getWaitingStatus(World level, Train train, NbtCompound tag) {
        int lastDisplaySnapshot = getLastDisplaySnapshot(tag);
        if (lastDisplaySnapshot == -1)
            return Text.empty();
        int offset = getOperator() == Ops.LESS ? -1 : getOperator() == Ops.GREATER ? 1 : 0;
        return Text.translatable(
            "create.schedule.condition.threshold.status",
            lastDisplaySnapshot,
            Math.max(0, getThreshold() + offset),
            Text.translatable("create.schedule.condition.threshold.buckets")
        );
    }
}
