package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.content.kinetics.mixer.CompactingRecipe;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CompactingDisplay extends CreateDisplay {
    public ItemStack result;
    public List<List<ItemStack>> ingredients;
    public @Nullable FluidIngredient fluidIngredient;

    public CompactingDisplay() {
    }

    public CompactingDisplay(RecipeHolder<CompactingRecipe> entry) {
        CompactingRecipe recipe = entry.value();
        result = recipe.result();
        ingredients = new ArrayList<>(recipe.ingredients().size());
        addSizedIngredient(recipe.ingredients(), ingredients);
        fluidIngredient = recipe.fluidIngredient();
    }

    @Override
    public void writeToTag(CompoundTag tag) {
        RegistryOps<Tag> ops = getServerOps();
        tag.store("result", ItemStack.CODEC, ops, result);
        if (fluidIngredient != null) {
            tag.store("fluidIngredient", FluidIngredient.CODEC, ops, fluidIngredient);
        }
        tag.store("ingredients", STACKS_LIST_CODEC, ops, ingredients);
    }

    @Override
    public void loadFromTag(CompoundTag tag) {
        RegistryOps<Tag> ops = getClientOps();
        result = tag.read("result", ItemStack.CODEC, ops).orElseThrow();
        fluidIngredient = tag.read("fluidIngredient", FluidIngredient.CODEC, ops).orElse(null);
        ingredients = tag.read("ingredients", STACKS_LIST_CODEC, ops).orElseThrow();
    }

    @Override
    public EivRecipeType<CompactingDisplay> getRecipeType() {
        return EivCommonPlugin.PACKING;
    }
}
