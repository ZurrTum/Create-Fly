package com.zurrtum.create.client.compat.jei.display;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;

import java.util.List;

public record BlockCuttingDisplay(Identifier id, Ingredient input, List<List<ItemStack>> outputs) {
}
