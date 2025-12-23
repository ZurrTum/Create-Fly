package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.content.kinetics.deployer.ItemApplicationRecipe;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import de.crafty.eiv.common.api.recipe.IEivServerRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class ManualApplicationDisplay extends CreateDisplay {
    public ItemStack result;
    public List<ItemStack> target;
    public List<ItemStack> ingredient;
    public boolean keepHeldItem;

    public ManualApplicationDisplay() {
    }

    public ManualApplicationDisplay(RecipeHolder<? extends ItemApplicationRecipe> entry) {
        ItemApplicationRecipe recipe = entry.value();
        result = recipe.result();
        target = getItemStacks(recipe.target());
        ingredient = getItemStacks(recipe.ingredient());
        keepHeldItem = recipe.keepHeldItem();
    }

    @Override
    public void writeToTag(CompoundTag tag) {
        RegistryOps<Tag> ops = getServerOps();
        tag.store("result", ItemStack.CODEC, ops, result);
        tag.store("target", STACKS_CODEC, ops, target);
        tag.store("ingredient", STACKS_CODEC, ops, ingredient);
        tag.putBoolean("keepHeldItem", keepHeldItem);
    }

    @Override
    public void loadFromTag(CompoundTag tag) {
        RegistryOps<Tag> ops = getClientOps();
        result = tag.read("result", ItemStack.CODEC, ops).orElseThrow();
        target = tag.read("target", STACKS_CODEC, ops).orElseThrow();
        ingredient = tag.read("ingredient", STACKS_CODEC, ops).orElseThrow();
        keepHeldItem = tag.getBooleanOr("keepHeldItem", false);
    }

    @Override
    public EivRecipeType<? extends IEivServerRecipe> getRecipeType() {
        return EivCommonPlugin.ITEM_APPLICATION;
    }
}
