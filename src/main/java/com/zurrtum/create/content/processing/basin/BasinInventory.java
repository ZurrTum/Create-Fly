package com.zurrtum.create.content.processing.basin;

import com.zurrtum.create.infrastructure.items.ItemInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;

public class BasinInventory implements ItemInventory {
    private final BasinBlockEntity blockEntity;
    private final DefaultedList<ItemStack> stacks = DefaultedList.ofSize(18, ItemStack.EMPTY);
    private boolean check = true;

    public BasinInventory(BasinBlockEntity be) {
        this.blockEntity = be;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        if (check) {
            if (slot > 9) {
                return false;
            }
            for (int i = 0; i < slot; i++) {
                ItemStack itemStack = stacks.get(i);
                if (itemStack.isEmpty() || ItemStack.areItemsAndComponentsEqual(itemStack, stack)) {
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
    public int size() {
        return 18;
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot >= 18) {
            return ItemStack.EMPTY;
        }
        return stacks.get(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot >= 18) {
            return;
        }
        stacks.set(slot, stack);
    }

    @Override
    public void markDirty() {
        blockEntity.notifyChangeOfContents();
        blockEntity.notifyUpdate();
    }

    public void write(WriteView view) {
        WriteView inventory = view.get("Inventory");
        WriteView.ListAppender<ItemStack> input = inventory.getListAppender("Input", ItemStack.CODEC);
        for (int i = 0; i < 9; i++) {
            ItemStack stack = stacks.get(i);
            if (stack.isEmpty()) {
                continue;
            }
            input.add(stack);
        }
        WriteView.ListAppender<ItemStack> output = inventory.getListAppender("Output", ItemStack.CODEC);
        for (int i = 9; i < 18; i++) {
            ItemStack stack = stacks.get(i);
            if (stack.isEmpty()) {
                continue;
            }
            output.add(stack);
        }
    }

    public void read(ReadView view) {
        view.getOptionalReadView("Inventory").ifPresentOrElse(
            inventory -> {
                List<ItemStack> list = inventory.getTypedListView("Input", ItemStack.CODEC).stream().toList();
                int stop = list.size();
                for (int i = 0; i < stop; i++) {
                    stacks.set(i, list.get(i));
                }
                for (int i = stop; i < 9; i++) {
                    stacks.set(i, ItemStack.EMPTY);
                }
                list = inventory.getTypedListView("Output", ItemStack.CODEC).stream().toList();
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
