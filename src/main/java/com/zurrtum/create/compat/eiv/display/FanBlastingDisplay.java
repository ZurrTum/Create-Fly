package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
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
        RecipeHolder<? extends AbstractCookingRecipe> entry,
        RegistryAccess registryManager,
        Collection<RecipeHolder<SmeltingRecipe>> smeltingRecipes,
        Collection<RecipeHolder<SmokingRecipe>> smokingRecipes
    ) {
        if (!AllRecipeTypes.CAN_BE_AUTOMATED.test(entry)) {
            return null;
        }
        AbstractCookingRecipe recipe = entry.value();
        HolderSet<Item> entries = recipe.input().values;
        if (entries instanceof HolderSet.Named<Item> named && !named.isBound()) {
            entries = registryManager.lookupOrThrow(Registries.ITEM).getOrThrow(named.key());
        }
        Optional<Holder<Item>> firstInput = entries.stream().findFirst();
        if (firstInput.isEmpty()) {
            return null;
        }
        ItemStack stack = firstInput.get().value().getDefaultInstance();
        if (smeltingRecipes != null) {
            Optional<RecipeHolder<SmeltingRecipe>> smeltingRecipe = smeltingRecipes.stream().filter(e -> e.value().input().test(stack)).findFirst()
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED);
            if (smeltingRecipe.isPresent()) {
                return null;
            }
        }
        Optional<RecipeHolder<SmokingRecipe>> smokingRecipe = smokingRecipes.stream().filter(e -> e.value().input().test(stack)).findFirst()
            .filter(AllRecipeTypes.CAN_BE_AUTOMATED);
        if (smokingRecipe.map(e -> ItemStack.isSameItem(e.value().result(), recipe.result())).orElse(false)) {
            return null;
        }
        return new FanBlastingDisplay(recipe.result(), recipe.input());
    }

    @Override
    public void writeToTag(CompoundTag tag) {
        RegistryOps<Tag> ops = getServerOps();
        tag.store("result", ItemStack.CODEC, ops, result);
        tag.store("ingredient", STACKS_CODEC, ops, ingredient);
    }

    @Override
    public void loadFromTag(CompoundTag tag) {
        RegistryOps<Tag> ops = getClientOps();
        result = tag.read("result", ItemStack.CODEC, ops).orElseThrow();
        ingredient = tag.read("ingredient", STACKS_CODEC, ops).orElseThrow();
    }

    @Override
    public EivRecipeType<FanBlastingDisplay> getRecipeType() {
        return EivCommonPlugin.FAN_BLASTING;
    }
}
