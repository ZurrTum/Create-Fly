package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.content.kinetics.crusher.AbstractCrushingRecipe;
import com.zurrtum.create.content.processing.recipe.ChanceOutput;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import de.crafty.eiv.common.api.recipe.IEivServerRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

public class CrushingDisplay extends CreateDisplay {
    public List<ItemStack> results;
    public List<Float> chances;
    public List<ItemStack> ingredient;

    public CrushingDisplay() {
    }

    public CrushingDisplay(RecipeHolder<? extends AbstractCrushingRecipe> entry) {
        AbstractCrushingRecipe recipe = entry.value();
        int size = recipe.results().size();
        results = new ArrayList<>(size);
        chances = new ArrayList<>(size);
        for (ChanceOutput output : recipe.results()) {
            results.add(output.stack());
            chances.add(output.chance());
        }
        ingredient = getItemStacks(recipe.ingredient());
    }

    @Override
    public void writeToTag(CompoundTag tag) {
        RegistryOps<Tag> ops = getServerOps();
        tag.store("results", STACKS_CODEC, ops, results);
        tag.store("chances", CreateCodecs.FLOAT_LIST_CODEC, ops, chances);
        tag.store("ingredient", STACKS_CODEC, ops, ingredient);
    }

    @Override
    public void loadFromTag(CompoundTag tag) {
        RegistryOps<Tag> ops = getClientOps();
        results = tag.read("results", STACKS_CODEC, ops).orElseThrow();
        chances = tag.read("chances", CreateCodecs.FLOAT_LIST_CODEC, ops).orElseThrow();
        ingredient = tag.read("ingredient", STACKS_CODEC, ops).orElseThrow();
    }

    @Override
    public EivRecipeType<? extends IEivServerRecipe> getRecipeType() {
        return EivCommonPlugin.CRUSHING;
    }
}
