package com.zurrtum.create.content.schematics.cannon;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.infrastructure.items.ItemInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SchematicannonInventory implements ItemInventory {
    private final SchematicannonBlockEntity blockEntity;
    private final NonNullList<ItemStack> stacks;

    public SchematicannonInventory(SchematicannonBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        this.stacks = NonNullList.withSize(5, ItemStack.EMPTY);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            // Blueprint Slot
            case 0 -> stack.is(AllItems.SCHEMATIC);
            // Blueprint output
            case 1 -> false;
            // Book input
            case 2 -> stack.is(AllItems.CLIPBOARD) || stack.is(Items.BOOK) || stack.is(Items.WRITTEN_BOOK);
            // Material List output
            case 3 -> false;
            // Gunpowder
            case 4 -> stack.is(Items.GUNPOWDER);
            default -> true;
        };
    }

    @Override
    public int getContainerSize() {
        return 5;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot >= 5) {
            return ItemStack.EMPTY;
        }
        return stacks.get(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 5) {
            return;
        }
        stacks.set(slot, stack);
    }

    @Override
    public void setChanged() {
        blockEntity.setChanged();
    }

    public void read(ValueInput view) {
        view.read("Inventory", CreateCodecs.ITEM_LIST_CODEC).ifPresentOrElse(
            list -> {
                for (int i = 0, size = list.size(); i < size; i++) {
                    stacks.set(i, list.get(i));
                }
            }, stacks::clear
        );
    }

    public void write(ValueOutput view) {
        view.store("Inventory", CreateCodecs.ITEM_LIST_CODEC, stacks);
    }
}
