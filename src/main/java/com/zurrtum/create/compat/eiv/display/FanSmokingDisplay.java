package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmokingRecipe;

import java.util.List;

public class FanSmokingDisplay extends CreateDisplay {
    public ItemStack result;
    public List<ItemStack> ingredient;

    public FanSmokingDisplay() {
    }

    public FanSmokingDisplay(RecipeHolder<SmokingRecipe> entry) {
        SmokingRecipe recipe = entry.value();
        result = recipe.result();
        ingredient = getItemStacks(recipe.input());
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
    public EivRecipeType<FanSmokingDisplay> getRecipeType() {
        return EivCommonPlugin.FAN_SMOKING;
    }
}
