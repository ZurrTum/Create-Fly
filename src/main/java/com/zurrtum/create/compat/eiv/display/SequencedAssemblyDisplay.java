package com.zurrtum.create.compat.eiv.display;

import com.mojang.serialization.Codec;
import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.content.processing.recipe.ChanceOutput;
import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyRecipe;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryOps;

import java.util.List;

public class SequencedAssemblyDisplay extends CreateDisplay {
    private static final Codec<List<Recipe<?>>> SEQUENCE_CODEC = Recipe.CODEC.listOf();
    public List<ItemStack> ingredient;
    public ChanceOutput result;
    public int loops;
    public List<Recipe<?>> sequence;

    public SequencedAssemblyDisplay() {
    }

    public SequencedAssemblyDisplay(RecipeEntry<SequencedAssemblyRecipe> entry) {
        SequencedAssemblyRecipe recipe = entry.value();
        ingredient = getItemStacks(recipe.ingredient());
        result = recipe.result();
        loops = recipe.loops();
        List<Recipe<?>> recipes = recipe.sequence();
        sequence = recipes.subList(0, recipes.size() / loops);
    }

    @Override
    public void writeToTag(NbtCompound tag) {
        RegistryOps<NbtElement> ops = getServerOps();
        tag.put("ingredient", STACKS_CODEC, ops, ingredient);
        tag.put("result", ChanceOutput.CODEC, ops, result);
        tag.putInt("loops", loops);
        tag.put("sequence", SEQUENCE_CODEC, ops, sequence);
    }

    @Override
    public void loadFromTag(NbtCompound tag) {
        RegistryOps<NbtElement> ops = getClientOps();
        ingredient = tag.get("ingredient", STACKS_CODEC, ops).orElseThrow();
        result = tag.get("result", ChanceOutput.CODEC, ops).orElseThrow();
        loops = tag.getInt("loops", 1);
        sequence = tag.get("sequence", SEQUENCE_CODEC, ops).orElseThrow();
    }

    @Override
    public EivRecipeType<SequencedAssemblyDisplay> getRecipeType() {
        return EivCommonPlugin.SEQUENCED_ASSEMBLY;
    }
}
