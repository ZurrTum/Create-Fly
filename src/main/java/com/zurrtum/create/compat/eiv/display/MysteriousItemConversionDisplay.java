package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import de.crafty.eiv.common.api.recipe.IEivServerRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MysteriousItemConversionDisplay extends CreateDisplay {
    public ItemStack ingredient;
    public ItemStack result;

    public MysteriousItemConversionDisplay() {
    }

    public MysteriousItemConversionDisplay(ItemStack ingredient, ItemStack result) {
        this.ingredient = ingredient;
        this.result = result;
    }

    public MysteriousItemConversionDisplay(Item ingredient, Item result) {
        this(ingredient.getDefaultInstance(), result.getDefaultInstance());
    }

    public static void register(List<IEivServerRecipe> recipes) {
        recipes.add(new MysteriousItemConversionDisplay(AllItems.EMPTY_BLAZE_BURNER, AllItems.BLAZE_BURNER));
        recipes.add(new MysteriousItemConversionDisplay(AllItems.PECULIAR_BELL, AllItems.HAUNTED_BELL));
    }

    @Override
    public void writeToTag(CompoundTag tag) {
        RegistryOps<Tag> ops = getServerOps();
        tag.store("ingredient", ItemStack.CODEC, ops, ingredient);
        tag.store("result", ItemStack.CODEC, ops, result);
    }

    @Override
    public void loadFromTag(CompoundTag nbtCompound) {
        RegistryOps<Tag> ops = getClientOps();
        ingredient = nbtCompound.read("ingredient", ItemStack.CODEC, ops).orElseThrow();
        result = nbtCompound.read("result", ItemStack.CODEC, ops).orElseThrow();
    }

    @Override
    public EivRecipeType<MysteriousItemConversionDisplay> getRecipeType() {
        return EivCommonPlugin.MYSTERY_CONVERSION;
    }
}
