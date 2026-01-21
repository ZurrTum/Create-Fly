package com.zurrtum.create.mixin;

import com.zurrtum.create.infrastructure.items.BaseInventory;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Container.class)
public interface ContainerMixin extends BaseInventory {
    @Shadow
    void setItem(int slot, ItemStack stack);

    @Shadow
    int getContainerSize();

    @Shadow
    int getMaxStackSize(ItemStack stack);

    @Shadow
    ItemStack getItem(int slot);

    @Shadow
    boolean canPlaceItem(int slot, ItemStack stack);

    @Shadow
    void setChanged();

    @Override
    default void create$setStack(int slot, @NonNull ItemStack stack) {
        setItem(slot, stack);
    }

    @Override
    default int create$size() {
        return getContainerSize();
    }

    @Override
    default int create$getMaxCount(@NonNull ItemStack stack) {
        return getMaxStackSize(stack);
    }

    @Override
    @NonNull
    default ItemStack create$getStack(int slot) {
        return getItem(slot);
    }

    @Override
    default boolean create$isValid(int slot, @NonNull ItemStack stack) {
        return canPlaceItem(slot, stack);
    }

    @Override
    default void create$markDirty() {
        setChanged();
    }
}
