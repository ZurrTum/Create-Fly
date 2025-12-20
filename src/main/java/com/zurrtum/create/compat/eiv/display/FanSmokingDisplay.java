package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.SmokingRecipe;
import net.minecraft.registry.RegistryOps;

import java.util.List;

public class FanSmokingDisplay extends CreateDisplay {
    public ItemStack result;
    public List<ItemStack> ingredient;

    public FanSmokingDisplay() {
    }

    public FanSmokingDisplay(RecipeEntry<SmokingRecipe> entry) {
        SmokingRecipe recipe = entry.value();
        result = recipe.result();
        ingredient = getItemStacks(recipe.ingredient());
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
    public EivRecipeType<FanSmokingDisplay> getRecipeType() {
        return EivCommonPlugin.FAN_SMOKING;
    }
}
