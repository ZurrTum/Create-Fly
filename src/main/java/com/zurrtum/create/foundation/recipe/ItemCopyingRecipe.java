package com.zurrtum.create.foundation.recipe;

import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.catnip.data.IntAttached;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class ItemCopyingRecipe extends CustomRecipe {

    public interface SupportsItemCopying {

        default ItemStack createCopy(ItemStack original, int count) {
            ItemStack copyWithCount = original.copyWithCount(count);
            copyWithCount.remove(DataComponents.ENCHANTMENTS);
            copyWithCount.remove(DataComponents.STORED_ENCHANTMENTS);
            return copyWithCount;
        }

        default boolean canCopyFromItem(ItemStack item) {
            return item.has(getComponentType());
        }

        default boolean canCopyToItem(ItemStack item) {
            return !item.has(getComponentType());
        }

        DataComponentType<?> getComponentType();

    }

    public ItemCopyingRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return copyCheck(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        IntAttached<ItemStack> copyCheck = copyCheck(input);
        if (copyCheck == null)
            return ItemStack.EMPTY;

        ItemStack itemToCopy = copyCheck.getValue();
        if (!(itemToCopy.getItem() instanceof SupportsItemCopying sic))
            return ItemStack.EMPTY;

        return sic.createCopy(itemToCopy, copyCheck.getFirst() + 1);
    }

    @Nullable
    private IntAttached<ItemStack> copyCheck(CraftingInput input) {
        ItemStack itemToCopy = ItemStack.EMPTY;
        int copyTargets = 0;

        int size = input.size();
        for (int j = 0; j < size; ++j) {
            ItemStack itemInSlot = input.getItem(j);
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
            ItemStack itemInSlot = input.getItem(j);
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
