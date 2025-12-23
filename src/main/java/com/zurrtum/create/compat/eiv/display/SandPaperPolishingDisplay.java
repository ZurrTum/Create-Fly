package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class SandPaperPolishingDisplay extends CreateDisplay {
    public ItemStack result;
    public List<ItemStack> ingredient;

    public SandPaperPolishingDisplay() {
    }

    public SandPaperPolishingDisplay(RecipeHolder<SandPaperPolishingRecipe> entry) {
        SandPaperPolishingRecipe recipe = entry.value();
        result = recipe.result();
        ingredient = getItemStacks(recipe.ingredient());
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
    public EivRecipeType<SandPaperPolishingDisplay> getRecipeType() {
        return EivCommonPlugin.SANDPAPER_POLISHING;
    }
}
