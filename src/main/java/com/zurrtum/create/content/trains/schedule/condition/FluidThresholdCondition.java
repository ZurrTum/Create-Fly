package com.zurrtum.create.content.trains.schedule.condition;

import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class FluidThresholdCondition extends CargoThresholdCondition {
    public FilterItemStack compareStack = FilterItemStack.empty();

    public FluidThresholdCondition(Identifier id) {
        super(id);
    }

    @Override
    protected boolean test(Level level, Train train, CompoundTag context) {
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
    protected void writeAdditional(ValueOutput view) {
        view.store("Bucket", FilterItemStack.CODEC, compareStack);
    }

    @Override
    protected void readAdditional(ValueInput view) {
        super.readAdditional(view);
        view.read("Bucket", FilterItemStack.CODEC).ifPresent(compareStack -> this.compareStack = compareStack);
    }

    @Override
    public MutableComponent getWaitingStatus(Level level, Train train, CompoundTag tag) {
        int lastDisplaySnapshot = getLastDisplaySnapshot(tag);
        if (lastDisplaySnapshot == -1)
            return Component.empty();
        int offset = getOperator() == Ops.LESS ? -1 : getOperator() == Ops.GREATER ? 1 : 0;
        return Component.translatable(
            "create.schedule.condition.threshold.status",
            lastDisplaySnapshot,
            Math.max(0, getThreshold() + offset),
            Component.translatable("create.schedule.condition.threshold.buckets")
        );
    }
}
