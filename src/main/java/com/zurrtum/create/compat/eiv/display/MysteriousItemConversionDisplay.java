package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import de.crafty.eiv.common.api.recipe.IEivServerRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryOps;

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
        this(ingredient.getDefaultStack(), result.getDefaultStack());
    }

    public static void register(List<IEivServerRecipe> recipes) {
        recipes.add(new MysteriousItemConversionDisplay(AllItems.EMPTY_BLAZE_BURNER, AllItems.BLAZE_BURNER));
        recipes.add(new MysteriousItemConversionDisplay(AllItems.PECULIAR_BELL, AllItems.HAUNTED_BELL));
    }

    @Override
    public void writeToTag(NbtCompound tag) {
        RegistryOps<NbtElement> ops = getServerOps();
        tag.put("ingredient", ItemStack.CODEC, ops, ingredient);
        tag.put("result", ItemStack.CODEC, ops, result);
    }

    @Override
    public void loadFromTag(NbtCompound nbtCompound) {
        RegistryOps<NbtElement> ops = getClientOps();
        ingredient = nbtCompound.get("ingredient", ItemStack.CODEC, ops).orElseThrow();
        result = nbtCompound.get("result", ItemStack.CODEC, ops).orElseThrow();
    }

    @Override
    public EivRecipeType<MysteriousItemConversionDisplay> getRecipeType() {
        return EivCommonPlugin.MYSTERY_CONVERSION;
    }
}
