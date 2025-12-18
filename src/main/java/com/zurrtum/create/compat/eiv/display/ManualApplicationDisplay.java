package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.content.kinetics.deployer.ItemApplicationRecipe;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import de.crafty.eiv.common.api.recipe.IEivServerRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryOps;

import java.util.List;

public class ManualApplicationDisplay extends CreateDisplay {
    public ItemStack result;
    public List<ItemStack> target;
    public List<ItemStack> ingredient;
    public boolean keepHeldItem;

    public ManualApplicationDisplay() {
    }

    public ManualApplicationDisplay(RecipeEntry<? extends ItemApplicationRecipe> entry) {
        ItemApplicationRecipe recipe = entry.value();
        result = recipe.result();
        target = getItemStacks(recipe.target());
        ingredient = getItemStacks(recipe.ingredient());
        keepHeldItem = recipe.keepHeldItem();
    }

    @Override
    public void writeToTag(NbtCompound tag) {
        RegistryOps<NbtElement> ops = getServerOps();
        tag.put("result", ItemStack.CODEC, ops, result);
        tag.put("target", STACKS_CODEC, ops, target);
        tag.put("ingredient", STACKS_CODEC, ops, ingredient);
        tag.putBoolean("keepHeldItem", keepHeldItem);
    }

    @Override
    public void loadFromTag(NbtCompound tag) {
        RegistryOps<NbtElement> ops = getClientOps();
        result = tag.get("result", ItemStack.CODEC, ops).orElseThrow();
        target = tag.get("target", STACKS_CODEC, ops).orElseThrow();
        ingredient = tag.get("ingredient", STACKS_CODEC, ops).orElseThrow();
        keepHeldItem = tag.getBoolean("keepHeldItem", false);
    }

    @Override
    public EivRecipeType<? extends IEivServerRecipe> getRecipeType() {
        return EivCommonPlugin.ITEM_APPLICATION;
    }
}
