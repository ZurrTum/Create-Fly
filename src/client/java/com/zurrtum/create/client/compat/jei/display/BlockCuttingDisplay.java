package com.zurrtum.create.client.compat.jei.display;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public record BlockCuttingDisplay(Identifier id, Ingredient input, List<List<ItemStack>> outputs) {
}
