package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.content.kinetics.press.MechanicalPressBlockEntity;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.registry.RegistryOps;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AutoMixingDisplay extends CreateDisplay {
    public List<List<ItemStack>> ingredients;
    public ItemStack result;

    public AutoMixingDisplay() {
    }

    public AutoMixingDisplay(ItemStack result, List<List<ItemStack>> ingredients) {
        this.ingredients = ingredients;
        this.result = result;
    }

    @Nullable
    public static AutoMixingDisplay of(RecipeEntry<CraftingRecipe> entry) {
        CraftingRecipe recipe = entry.value();
        if (!(recipe instanceof ShapelessRecipe shapelessRecipe) || MechanicalPressBlockEntity.canCompress(shapelessRecipe) || AllRecipeTypes.shouldIgnoreInAutomation(
            entry) || shapelessRecipe.ingredients.size() == 1) {
            return null;
        }
        Object2IntMap<Ingredient> map = new Object2IntArrayMap<>();
        Iterator<Ingredient> iterator = shapelessRecipe.ingredients.iterator();
        map.put(iterator.next(), 1);
        Find:
        do {
            Ingredient ingredient = iterator.next();
            for (Object2IntMap.Entry<Ingredient> pair : map.object2IntEntrySet()) {
                if (pair.getKey().equals(ingredient)) {
                    pair.setValue(pair.getIntValue() + 1);
                    continue Find;
                }
            }
            map.put(ingredient, 1);
        } while (iterator.hasNext());
        List<List<ItemStack>> ingredients = new ArrayList<>(map.size());
        addSizedIngredient(map, ingredients);
        return new AutoMixingDisplay(shapelessRecipe.result, ingredients);
    }

    @Override
    public void writeToTag(NbtCompound tag) {
        RegistryOps<NbtElement> ops = getServerOps();
        tag.put("result", ItemStack.CODEC, ops, result);
        tag.put("ingredient", STACKS_LIST_CODEC, ops, ingredients);
    }

    @Override
    public void loadFromTag(NbtCompound tag) {
        RegistryOps<NbtElement> ops = getClientOps();
        result = tag.get("result", ItemStack.CODEC, ops).orElseThrow();
        ingredients = tag.get("ingredient", STACKS_LIST_CODEC, ops).orElseThrow();
    }

    @Override
    public EivRecipeType<AutoMixingDisplay> getRecipeType() {
        return EivCommonPlugin.AUTOMATIC_SHAPELESS;
    }
}
