package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.content.kinetics.crusher.CrushingRecipe;
import com.zurrtum.create.content.kinetics.millstone.MillingRecipe;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRollableRecipe;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import de.crafty.eiv.common.api.recipe.IEivServerRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CrushingDisplay extends CreateDisplay {
    public @UnknownNullability List<ItemStack> results;
    public @UnknownNullability List<Float> chances;
    public @UnknownNullability List<ItemStack> ingredient;

    public CrushingDisplay() {
    }

    public CrushingDisplay(RecipeHolder<? extends CreateSingleStackRollableRecipe> entry) {
        CreateSingleStackRollableRecipe recipe = entry.value();
        List<ProcessingOutput> outputs = recipe.results();
        int size = outputs.size();
        results = new ArrayList<>(size);
        chances = new ArrayList<>(size);
        for (ProcessingOutput output : outputs) {
            results.add(output.create());
            chances.add(output.chance());
        }
        ingredient = getItemStacks(recipe.ingredient());
    }

    @Nullable
    public static CrushingDisplay of(
        RecipeHolder<MillingRecipe> entry,
        Collection<RecipeHolder<CrushingRecipe>> crushingRecipes
    ) {
        ItemStack firstInput = getFirstStack(entry.value().ingredient());
        if (!firstInput.isEmpty() && crushingRecipes.stream().anyMatch(e -> e.value().ingredient().test(firstInput))) {
            return null;
        }
        return new CrushingDisplay(entry);
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
