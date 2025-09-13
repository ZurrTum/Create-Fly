package com.zurrtum.create.content.schematics.cannon;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.infrastructure.items.ItemInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.collection.DefaultedList;

public class SchematicannonInventory implements ItemInventory {
    private final SchematicannonBlockEntity blockEntity;
    private final DefaultedList<ItemStack> stacks;

    public SchematicannonInventory(SchematicannonBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        this.stacks = DefaultedList.ofSize(5, ItemStack.EMPTY);
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return switch (slot) {
            // Blueprint Slot
            case 0 -> stack.isOf(AllItems.SCHEMATIC);
            // Blueprint output
            case 1 -> false;
            // Book input
            case 2 -> stack.isOf(AllItems.CLIPBOARD) || stack.isOf(Items.BOOK) || stack.isOf(Items.WRITTEN_BOOK);
            // Material List output
            case 3 -> false;
            // Gunpowder
            case 4 -> stack.isOf(Items.GUNPOWDER);
            default -> true;
        };
    }

    @Override
    public int size() {
        return 5;
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot >= 5) {
            return ItemStack.EMPTY;
        }
        return stacks.get(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot >= 5) {
            return;
        }
        stacks.set(slot, stack);
    }

    @Override
    public void markDirty() {
        blockEntity.markDirty();
    }

    public void read(ReadView view) {
        view.read("Inventory", CreateCodecs.ITEM_LIST_CODEC).ifPresentOrElse(
            list -> {
                for (int i = 0, size = list.size(); i < size; i++) {
                    stacks.set(i, list.get(i));
                }
            }, stacks::clear
        );
    }

    public void write(WriteView view) {
        view.put("Inventory", CreateCodecs.ITEM_LIST_CODEC, stacks);
    }
}
