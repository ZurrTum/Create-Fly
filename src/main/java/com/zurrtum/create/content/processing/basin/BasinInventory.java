package com.zurrtum.create.content.processing.basin;

import com.zurrtum.create.infrastructure.items.ItemInventory;

import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BasinInventory implements ItemInventory {
    private final BasinBlockEntity blockEntity;
    private final NonNullList<ItemStack> stacks = NonNullList.withSize(18, ItemStack.EMPTY);
    private boolean check = true;

    public BasinInventory(BasinBlockEntity be) {
        this.blockEntity = be;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (check) {
            if (slot > 9) {
                return false;
            }
            for (int i = 0; i < slot; i++) {
                ItemStack itemStack = stacks.get(i);
                if (itemStack.isEmpty() || ItemStack.isSameItemSameComponents(itemStack, stack)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void disableCheck() {
        check = false;
    }

    public void enableCheck() {
        check = true;
    }

    @Override
    public int getContainerSize() {
        return 18;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot >= 18) {
            return ItemStack.EMPTY;
        }
        return stacks.get(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 18) {
            return;
        }
        stacks.set(slot, stack);
    }

    @Override
    public void setChanged() {
        blockEntity.notifyChangeOfContents();
        blockEntity.notifyUpdate();
    }

    public void write(ValueOutput view) {
        ValueOutput inventory = view.child("Inventory");
        ValueOutput.TypedOutputList<ItemStack> input = inventory.list("Input", ItemStack.CODEC);
        for (int i = 0; i < 9; i++) {
            ItemStack stack = stacks.get(i);
            if (stack.isEmpty()) {
                continue;
            }
            input.add(stack);
        }
        ValueOutput.TypedOutputList<ItemStack> output = inventory.list("Output", ItemStack.CODEC);
        for (int i = 9; i < 18; i++) {
            ItemStack stack = stacks.get(i);
            if (stack.isEmpty()) {
                continue;
            }
            output.add(stack);
        }
    }

    public void read(ValueInput view) {
        view.child("Inventory").ifPresentOrElse(
            inventory -> {
                List<ItemStack> list = inventory.listOrEmpty("Input", ItemStack.CODEC).stream().toList();
                int stop = list.size();
                for (int i = 0; i < stop; i++) {
                    stacks.set(i, list.get(i));
                }
                for (int i = stop; i < 9; i++) {
                    stacks.set(i, ItemStack.EMPTY);
                }
                list = inventory.listOrEmpty("Output", ItemStack.CODEC).stream().toList();
                stop = 9 + list.size();
                for (int i = 9; i < stop; i++) {
                    stacks.set(i, list.get(i - 9));
                }
                for (int i = stop; i < 18; i++) {
                    stacks.set(i, ItemStack.EMPTY);
                }
            }, stacks::clear
        );
    }
}
