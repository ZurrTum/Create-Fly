package com.zurrtum.create.compat.rei;

import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.kinetics.mixer.CompactingRecipe;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import com.zurrtum.create.content.kinetics.press.PressingRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.plugin.common.displays.crafting.CraftingDisplay;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.ShapelessRecipe;

import static com.zurrtum.create.Create.MOD_ID;

public class ReiCommonPlugin implements REICommonPlugin {
    public static final CategoryIdentifier<CraftingDisplay> AUTOMATIC_PACKING = CategoryIdentifier.of(MOD_ID, "automatic_packing");
    public static final CategoryIdentifier<CompactingDisplay> PACKING = CategoryIdentifier.of(MOD_ID, "packing");
    public static final CategoryIdentifier<PressingDisplay> PRESSING = CategoryIdentifier.of(MOD_ID, "pressing");
    public static final CategoryIdentifier<CraftingDisplay> AUTOMATIC_SHAPELESS = CategoryIdentifier.of(MOD_ID, "automatic_shapeless");
    public static final CategoryIdentifier<MixingDisplay> MIXING = CategoryIdentifier.of(MOD_ID, "mixing");

    @Override
    public void registerDisplays(ServerDisplayRegistry registry) {
        registry.beginRecipeFiller(CraftingRecipe.class).fill(AutoCompactingDisplay::of);
        registry.beginRecipeFiller(CompactingRecipe.class).filterType(AllRecipeTypes.COMPACTING).fill(CompactingDisplay::new);
        registry.beginRecipeFiller(PressingRecipe.class).filterType(AllRecipeTypes.PRESSING).fill(PressingDisplay::new);
        registry.beginRecipeFiller(ShapelessRecipe.class).fill(AutoMixingDisplay::of);
        registry.beginRecipeFiller(MixingRecipe.class).fill(MixingDisplay::new);
    }

    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(
            AUTOMATIC_PACKING.getIdentifier().withSuffixedPath("/default/shapeless"),
            AutoCompactingDisplay.ShapelessDisplay.SERIALIZER
        );
        registry.register(AUTOMATIC_PACKING.getIdentifier().withSuffixedPath("/default/shaped"), AutoCompactingDisplay.ShapedDisplay.SERIALIZER);
        registry.register(
            AUTOMATIC_PACKING.getIdentifier().withSuffixedPath("/client/shaped"),
            AutoCompactingDisplay.CraftingDisplayShaped.SERIALIZER
        );
        registry.register(
            AUTOMATIC_PACKING.getIdentifier().withSuffixedPath("/client/shapeless"),
            AutoCompactingDisplay.CraftingDisplayShapeless.SERIALIZER
        );
        registry.register(PACKING.getIdentifier(), CompactingDisplay.SERIALIZER);
        registry.register(PRESSING.getIdentifier(), PressingDisplay.SERIALIZER);
        registry.register(AUTOMATIC_SHAPELESS.getIdentifier().withSuffixedPath("/default/shapeless"), AutoMixingDisplay.ShapelessDisplay.SERIALIZER);
        registry.register(
            AUTOMATIC_SHAPELESS.getIdentifier().withSuffixedPath("/client/shapeless"),
            AutoMixingDisplay.CraftingDisplayShapeless.SERIALIZER
        );
        registry.register(MIXING.getIdentifier(), MixingDisplay.SERIALIZER);
    }
}
