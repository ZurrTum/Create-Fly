package com.zurrtum.create.client.compat.jei.display;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public record BlockCuttingDisplay(ResourceLocation id, Ingredient input, List<List<ItemStack>> outputs) {
}
