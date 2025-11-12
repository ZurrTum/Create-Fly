package com.zurrtum.create.content.trains.schedule.condition;

import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ItemThresholdCondition extends CargoThresholdCondition {
    public FilterItemStack stack = FilterItemStack.empty();

    public ItemThresholdCondition(ResourceLocation id) {
        super(id);
    }

    @Override
    protected boolean test(Level level, Train train, CompoundTag context) {
        Ops operator = getOperator();
        int target = getThreshold();
        boolean stacks = inStacks();

        int foundItems = 0;
        for (Carriage carriage : train.carriages) {
            Container items = carriage.storage.getAllItems();
            for (int i = 0, size = items.getContainerSize(); i < size; i++) {
                ItemStack stackInSlot = items.getItem(i);
                if (!stack.test(level, stackInSlot))
                    continue;

                if (stacks)
                    foundItems += stackInSlot.getCount() == stackInSlot.getMaxStackSize() ? 1 : 0;
                else
                    foundItems += stackInSlot.getCount();
            }
        }

        requestStatusToUpdate(foundItems, context);
        return operator.test(foundItems, target);
    }

    @Override
    protected void writeAdditional(ValueOutput view) {
        super.writeAdditional(view);
        view.store("Item", FilterItemStack.CODEC, stack);
    }

    @Override
    protected void readAdditional(ValueInput view) {
        super.readAdditional(view);
        view.read("Item", FilterItemStack.CODEC).ifPresent(stack -> this.stack = stack);
    }

    @Override
    public boolean tickCompletion(Level level, Train train, CompoundTag context) {
        return super.tickCompletion(level, train, context);
    }

    public boolean inStacks() {
        return intData("Measure") == 1;
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
            Component.translatable("create.schedule.condition.threshold." + (inStacks() ? "stacks" : "items"))
        );
    }
}
