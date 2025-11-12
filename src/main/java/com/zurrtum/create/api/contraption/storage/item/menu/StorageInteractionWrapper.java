package com.zurrtum.create.api.contraption.storage.item.menu;

import com.zurrtum.create.infrastructure.items.ItemInventory;

import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.world.Container;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class StorageInteractionWrapper implements ItemInventory {
    private final Container inv;
    private final Predicate<Player> stillValid;
    private final Consumer<ContainerUser> onClose;

    public StorageInteractionWrapper(Container inv, Predicate<Player> stillValid, Consumer<ContainerUser> onClose) {
        this.inv = inv;
        this.stillValid = stillValid;
        this.onClose = onClose;
    }

    @Override
    public int getContainerSize() {
        return inv.getContainerSize();
    }

    @Override
    public ItemStack getItem(int slot) {
        return inv.getItem(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        inv.setItem(slot, stack);
    }

    @Override
    public int getMaxStackSize() {
        return inv.getMaxStackSize();
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return inv.getMaxStackSize(stack);
    }

    @Override
    public int insert(ItemStack stack) {
        return inv.insert(stack);
    }

    @Override
    public int extract(ItemStack stack) {
        return inv.extract(stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid.test(player);
    }

    @Override
    public void stopOpen(ContainerUser player) {
        onClose.accept(player);
    }
}
