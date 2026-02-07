package com.zurrtum.create.foundation.recipe;

import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.catnip.data.IntAttached;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ItemCopyingRecipe extends SpecialCraftingRecipe {

    public interface SupportsItemCopying {

        default ItemStack createCopy(ItemStack original, int count) {
            ItemStack copyWithCount = original.copyWithCount(count);
            copyWithCount.set(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
            copyWithCount.remove(DataComponentTypes.STORED_ENCHANTMENTS);
            return copyWithCount;
        }

        default boolean canCopyFromItem(ItemStack item) {
            return item.contains(getComponentType());
        }

        default boolean canCopyToItem(ItemStack item) {
            return !item.contains(getComponentType());
        }

        ComponentType<?> getComponentType();

    }

    public ItemCopyingRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World level) {
        return copyCheck(input) != null;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        IntAttached<ItemStack> copyCheck = copyCheck(input);
        if (copyCheck == null)
            return ItemStack.EMPTY;

        ItemStack itemToCopy = copyCheck.getValue();
        if (!(itemToCopy.getItem() instanceof SupportsItemCopying sic))
            return ItemStack.EMPTY;

        return sic.createCopy(itemToCopy, copyCheck.getFirst() + 1);
    }

    @Nullable
    private IntAttached<ItemStack> copyCheck(CraftingRecipeInput input) {
        ItemStack itemToCopy = ItemStack.EMPTY;
        int copyTargets = 0;

        int size = input.size();
        for (int j = 0; j < size; ++j) {
            ItemStack itemInSlot = input.getStackInSlot(j);
            if (itemInSlot.isEmpty())
                continue;
            if (!(itemInSlot.getItem() instanceof SupportsItemCopying sic))
                return null;
            if (!sic.canCopyFromItem(itemInSlot))
                continue;
            itemToCopy = itemInSlot;
            break;
        }
        if (itemToCopy.isEmpty())
            return null;

        for (int j = 0; j < size; ++j) {
            ItemStack itemInSlot = input.getStackInSlot(j);
            if (itemInSlot.isEmpty() || itemInSlot == itemToCopy)
                continue;
            if (itemToCopy.getItem() != itemInSlot.getItem())
                return null;
            if (!(itemInSlot.getItem() instanceof SupportsItemCopying sic))
                return null;
            if (sic.canCopyFromItem(itemInSlot))
                return null;
            if (!sic.canCopyToItem(itemInSlot))
                return null;
            copyTargets++;
        }
        if (copyTargets == 0)
            return null;

        return IntAttached.with(copyTargets, itemToCopy);
    }

    @Override
    public RecipeSerializer<ItemCopyingRecipe> getSerializer() {
        return AllRecipeSerializers.ITEM_COPYING;
    }
}
