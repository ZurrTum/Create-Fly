package com.zurrtum.create.compat.eiv.display;

import com.mojang.serialization.Codec;
import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.content.processing.recipe.ChanceOutput;
import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyRecipe;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class SequencedAssemblyDisplay extends CreateDisplay {
    private static final Codec<List<Recipe<?>>> SEQUENCE_CODEC = Recipe.CODEC.listOf();
    public List<ItemStack> ingredient;
    public ChanceOutput result;
    public int loops;
    public List<Recipe<?>> sequence;

    public SequencedAssemblyDisplay() {
    }

    public SequencedAssemblyDisplay(RecipeHolder<SequencedAssemblyRecipe> entry) {
        SequencedAssemblyRecipe recipe = entry.value();
        ingredient = getItemStacks(recipe.ingredient());
        result = recipe.result();
        loops = recipe.loops();
        List<Recipe<?>> recipes = recipe.sequence();
        sequence = recipes.subList(0, recipes.size() / loops);
    }

    @Override
    public void writeToTag(CompoundTag tag) {
        RegistryOps<Tag> ops = getServerOps();
        tag.store("ingredient", STACKS_CODEC, ops, ingredient);
        tag.store("result", ChanceOutput.CODEC, ops, result);
        tag.putInt("loops", loops);
        tag.store("sequence", SEQUENCE_CODEC, ops, sequence);
    }

    @Override
    public void loadFromTag(CompoundTag tag) {
        RegistryOps<Tag> ops = getClientOps();
        ingredient = tag.read("ingredient", STACKS_CODEC, ops).orElseThrow();
        result = tag.read("result", ChanceOutput.CODEC, ops).orElseThrow();
        loops = tag.getIntOr("loops", 1);
        sequence = tag.read("sequence", SEQUENCE_CODEC, ops).orElseThrow();
    }

    @Override
    public EivRecipeType<SequencedAssemblyDisplay> getRecipeType() {
        return EivCommonPlugin.SEQUENCED_ASSEMBLY;
    }
}
