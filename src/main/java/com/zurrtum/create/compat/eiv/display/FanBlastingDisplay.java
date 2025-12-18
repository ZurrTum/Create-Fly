package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.recipe.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class FanBlastingDisplay extends CreateDisplay {
    public ItemStack result;
    public List<ItemStack> ingredient;

    public FanBlastingDisplay() {
    }

    public FanBlastingDisplay(ItemStack result, Ingredient ingredient) {
        this.result = result;
        this.ingredient = getItemStacks(ingredient);
    }

    @Nullable
    public static FanBlastingDisplay of(
        RecipeEntry<? extends AbstractCookingRecipe> entry,
        DynamicRegistryManager registryManager,
        Collection<RecipeEntry<SmeltingRecipe>> smeltingRecipes,
        Collection<RecipeEntry<SmokingRecipe>> smokingRecipes
    ) {
        if (!AllRecipeTypes.CAN_BE_AUTOMATED.test(entry)) {
            return null;
        }
        AbstractCookingRecipe recipe = entry.value();
        RegistryEntryList<Item> entries = recipe.ingredient().entries;
        if (entries instanceof RegistryEntryList.Named<Item> named && !named.isBound()) {
            entries = registryManager.getOrThrow(RegistryKeys.ITEM).getOrThrow(named.getTag());
        }
        Optional<RegistryEntry<Item>> firstInput = entries.stream().findFirst();
        if (firstInput.isEmpty()) {
            return null;
        }
        ItemStack stack = firstInput.get().value().getDefaultStack();
        if (smeltingRecipes != null) {
            Optional<RecipeEntry<SmeltingRecipe>> smeltingRecipe = smeltingRecipes.stream().filter(e -> e.value().ingredient().test(stack))
                .findFirst().filter(AllRecipeTypes.CAN_BE_AUTOMATED);
            if (smeltingRecipe.isPresent()) {
                return null;
            }
        }
        Optional<RecipeEntry<SmokingRecipe>> smokingRecipe = smokingRecipes.stream().filter(e -> e.value().ingredient().test(stack)).findFirst()
            .filter(AllRecipeTypes.CAN_BE_AUTOMATED);
        if (smokingRecipe.map(e -> ItemStack.areItemsEqual(e.value().result(), recipe.result())).orElse(false)) {
            return null;
        }
        return new FanBlastingDisplay(recipe.result(), recipe.ingredient());
    }

    @Override
    public void writeToTag(NbtCompound tag) {
        RegistryOps<NbtElement> ops = getServerOps();
        tag.put("result", ItemStack.CODEC, ops, result);
        tag.put("ingredient", STACKS_CODEC, ops, ingredient);
    }

    @Override
    public void loadFromTag(NbtCompound tag) {
        RegistryOps<NbtElement> ops = getClientOps();
        result = tag.get("result", ItemStack.CODEC, ops).orElseThrow();
        ingredient = tag.get("ingredient", STACKS_CODEC, ops).orElseThrow();
    }

    @Override
    public EivRecipeType<FanBlastingDisplay> getRecipeType() {
        return EivCommonPlugin.FAN_BLASTING;
    }
}
