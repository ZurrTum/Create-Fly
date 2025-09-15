package com.zurrtum.create.compat.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.plugin.common.displays.crafting.CraftingDisplay;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeType;

import static com.zurrtum.create.Create.MOD_ID;

public class ReiCommonPlugin implements REICommonPlugin {
    public static final CategoryIdentifier<CraftingDisplay> AUTO_COMPACTING = CategoryIdentifier.of(MOD_ID, "auto_compacting");

    @Override
    public void registerDisplays(ServerDisplayRegistry registry) {
        registry.beginRecipeFiller(CraftingRecipe.class).filterType(RecipeType.CRAFTING).fill(AutoCompactingDisplay::of);
    }

    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(AUTO_COMPACTING.getIdentifier().withSuffixedPath("/default/shapeless"), AutoCompactingDisplay.ShapelessDisplay.SERIALIZER);
        registry.register(AUTO_COMPACTING.getIdentifier().withSuffixedPath("/default/shaped"), AutoCompactingDisplay.ShapedDisplay.SERIALIZER);
        registry.register(AUTO_COMPACTING.getIdentifier().withSuffixedPath("/client/shaped"), AutoCompactingDisplay.CraftingDisplayShaped.SERIALIZER);
        registry.register(
            AUTO_COMPACTING.getIdentifier().withSuffixedPath("/client/shapeless"),
            AutoCompactingDisplay.CraftingDisplayShapeless.SERIALIZER
        );
    }
}
