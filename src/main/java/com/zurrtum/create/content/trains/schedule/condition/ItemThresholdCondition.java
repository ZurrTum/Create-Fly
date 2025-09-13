package com.zurrtum.create.content.trains.schedule.condition;

import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ItemThresholdCondition extends CargoThresholdCondition {
    public FilterItemStack stack = FilterItemStack.empty();

    public ItemThresholdCondition(Identifier id) {
        super(id);
    }

    @Override
    protected boolean test(World level, Train train, NbtCompound context) {
        Ops operator = getOperator();
        int target = getThreshold();
        boolean stacks = inStacks();

        int foundItems = 0;
        for (Carriage carriage : train.carriages) {
            Inventory items = carriage.storage.getAllItems();
            for (int i = 0, size = items.size(); i < size; i++) {
                ItemStack stackInSlot = items.getStack(i);
                if (!stack.test(level, stackInSlot))
                    continue;

                if (stacks)
                    foundItems += stackInSlot.getCount() == stackInSlot.getMaxCount() ? 1 : 0;
                else
                    foundItems += stackInSlot.getCount();
            }
        }

        requestStatusToUpdate(foundItems, context);
        return operator.test(foundItems, target);
    }

    @Override
    protected void writeAdditional(WriteView view) {
        super.writeAdditional(view);
        view.put("Item", FilterItemStack.CODEC, stack);
    }

    @Override
    protected void readAdditional(ReadView view) {
        super.readAdditional(view);
        view.read("Item", FilterItemStack.CODEC).ifPresent(stack -> this.stack = stack);
    }

    @Override
    public boolean tickCompletion(World level, Train train, NbtCompound context) {
        return super.tickCompletion(level, train, context);
    }

    public boolean inStacks() {
        return intData("Measure") == 1;
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
            Text.translatable("create.schedule.condition.threshold." + (inStacks() ? "stacks" : "items"))
        );
    }
}
