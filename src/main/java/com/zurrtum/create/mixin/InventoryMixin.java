package com.zurrtum.create.mixin;

import com.zurrtum.create.infrastructure.items.BaseInventory;
import com.zurrtum.create.infrastructure.items.SidedItemInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;

@Mixin(Inventory.class)
public interface InventoryMixin extends BaseInventory {
    @Shadow
    void setStack(int slot, ItemStack stack);

    @Shadow
    int size();

    @Shadow
    int getMaxCount(ItemStack stack);

    @Shadow
    ItemStack getStack(int slot);

    @Shadow
    boolean isValid(int slot, ItemStack stack);

    @Shadow
    Iterator<ItemStack> iterator();

    @Shadow
    void markDirty();

    @Override
    default void create$setStack(int slot, ItemStack stack) {
        setStack(slot, stack);
    }

    @Override
    default int create$size() {
        return size();
    }

    @Override
    default int create$getMaxCount(ItemStack stack) {
        return getMaxCount(stack);
    }

    @Override
    default ItemStack create$getStack(int slot) {
        return getStack(slot);
    }

    @Override
    default boolean create$isValid(int slot, ItemStack stack) {
        return isValid(slot, stack);
    }

    @Override
    default Iterator<ItemStack> create$iterator() {
        return iterator();
    }

    @Override
    default void create$markDirty() {
        markDirty();
    }

    @Inject(method = "iterator()Ljava/util/Iterator;", at = @At("HEAD"), cancellable = true)
    private void checkSideInventory(CallbackInfoReturnable<Iterator<ItemStack>> cir) {
        if (this instanceof SidedItemInventory inventory) {
            cir.setReturnValue(inventory.iterator(null));
        }
    }
}
