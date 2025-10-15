package com.zurrtum.create.infrastructure.items;

import com.zurrtum.create.infrastructure.transfer.SlotRangeCache;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Predicate;

public record SidedInventoryWrapper(Inventory inventory, int[] slots) implements SidedItemInventory {
    public SidedInventoryWrapper(Inventory inventory) {
        this(inventory, SlotRangeCache.get(inventory.size()));
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return slots;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return true;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        return inventory.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.getStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return inventory.removeStack(slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return inventory.removeStack(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.setStack(slot, stack);
    }

    @Override
    public int getMaxCountPerStack() {
        return inventory.getMaxCountPerStack();
    }

    @Override
    public int getMaxCount(ItemStack stack) {
        return inventory.getMaxCount(stack);
    }

    @Override
    public void markDirty() {
        inventory.markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    @Override
    public void onOpen(PlayerEntity player) {
        inventory.onOpen(player);
    }

    @Override
    public void onClose(PlayerEntity player) {
        inventory.onClose(player);
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return inventory.isValid(slot, stack);
    }

    @Override
    public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
        return inventory.canTransferTo(hopperInventory, slot, stack);
    }

    @Override
    public int count(Item item) {
        return inventory.count(item);
    }

    @Override
    public boolean containsAny(Set<Item> items) {
        return inventory.containsAny(items);
    }

    @Override
    public boolean containsAny(Predicate<ItemStack> predicate) {
        return inventory.containsAny(predicate);
    }

    @Override
    public int insert(ItemStack stack) {
        return inventory.insert(stack);
    }

    @Override
    public int insert(ItemStack stack, Direction side) {
        return inventory.insert(stack);
    }

    @Override
    public boolean preciseInsert(ItemStack stack) {
        return inventory.preciseInsert(stack);
    }

    @Override
    public boolean preciseInsert(ItemStack stack, Direction side) {
        return inventory.preciseInsert(stack);
    }

    @Override
    public int count(ItemStack stack) {
        return inventory.count(stack);
    }

    @Override
    public int count(ItemStack stack, Direction side) {
        return inventory.count(stack);
    }

    @Override
    public int count(ItemStack stack, int maxAmount) {
        return inventory.count(stack, maxAmount);
    }

    @Override
    public int count(ItemStack stack, int maxAmount, Direction side) {
        return inventory.count(stack, maxAmount);
    }

    @Override
    public int countSpace(ItemStack stack) {
        return inventory.countSpace(stack);
    }

    @Override
    public int countSpace(ItemStack stack, Direction side) {
        return inventory.countSpace(stack);
    }

    @Override
    public int countSpace(ItemStack stack, int maxAmount) {
        return inventory.countSpace(stack, maxAmount);
    }

    @Override
    public int countSpace(ItemStack stack, int maxAmount, Direction side) {
        return inventory.countSpace(stack, maxAmount);
    }

    @Override
    public int extract(ItemStack stack) {
        return inventory.extract(stack);
    }

    @Override
    public int extract(ItemStack stack, Direction side) {
        return inventory.extract(stack);
    }

    @Override
    public boolean preciseExtract(ItemStack stack) {
        return inventory.preciseExtract(stack);
    }

    @Override
    public boolean preciseExtract(ItemStack stack, Direction side) {
        return inventory.preciseExtract(stack);
    }

    @Override
    @NotNull
    public java.util.Iterator<ItemStack> iterator() {
        return inventory.iterator();
    }

    @Override
    @NotNull
    public java.util.Iterator<ItemStack> iterator(Direction side) {
        return inventory.iterator();
    }
}
