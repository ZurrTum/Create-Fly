package com.zurrtum.create.compat.rei;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.kinetics.press.MechanicalPressBlockEntity;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.client.displays.ClientsidedCraftingDisplay;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCustomShapelessDisplay;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.NetworkRecipeId;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public interface AutoMixingDisplay {
    @SuppressWarnings("unchecked")
    static Display of(RecipeEntry<?> entry) {
        Recipe<?> recipe = entry.value();
        if (MechanicalPressBlockEntity.canCompress(recipe) || AllRecipeTypes.shouldIgnoreInAutomation(entry)) {
            return null;
        }
        if (recipe instanceof ShapelessRecipe shapelessRecipe) {
            if (shapelessRecipe.ingredients.size() > 1) {
                return new ShapelessDisplay((RecipeEntry<ShapelessRecipe>) entry);
            }
        } else if (!recipe.isIgnoredInRecipeBook()) {
            for (RecipeDisplay d : recipe.getDisplays()) {
                if (d instanceof ShapelessCraftingRecipeDisplay display && display.ingredients().size() > 1) {
                    return new CraftingDisplayShapeless(display);
                }
            }
        }
        return null;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    class ShapelessDisplay extends DefaultCustomShapelessDisplay implements AutoMixingDisplay {
        public static final DisplaySerializer<ShapelessDisplay> SERIALIZER = DisplaySerializer.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(DefaultCraftingDisplay::getInputEntries),
                EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(DefaultCraftingDisplay::getOutputEntries),
                Identifier.CODEC.optionalFieldOf("location").forGetter(DefaultCraftingDisplay::getDisplayLocation)
            ).apply(instance, ShapelessDisplay::new)), PacketCodec.tuple(
                EntryIngredient.streamCodec().collect(PacketCodecs.toList()),
                DefaultCraftingDisplay::getInputEntries,
                EntryIngredient.streamCodec().collect(PacketCodecs.toList()),
                DefaultCraftingDisplay::getOutputEntries,
                PacketCodecs.optional(Identifier.PACKET_CODEC),
                DefaultCraftingDisplay::getDisplayLocation,
                ShapelessDisplay::new
            )
        );

        public ShapelessDisplay(List<EntryIngredient> input, List<EntryIngredient> output, Optional<Identifier> location) {
            super(input, output, location);
        }

        public ShapelessDisplay(RecipeEntry<ShapelessRecipe> recipe) {
            super(
                CollectionUtils.map(recipe.value().getIngredientPlacement().getIngredients(), EntryIngredients::ofIngredient),
                List.of(EntryIngredients.of(recipe.value().result)),
                Optional.of(recipe.id().getValue())
            );
        }

        @Override
        public CategoryIdentifier<?> getCategoryIdentifier() {
            return ReiCommonPlugin.AUTOMATIC_SHAPELESS;
        }

        @Override
        public DisplaySerializer<? extends Display> getSerializer() {
            return SERIALIZER;
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    class CraftingDisplayShapeless extends ClientsidedCraftingDisplay.Shapeless implements AutoMixingDisplay {
        public static final DisplaySerializer<CraftingDisplayShapeless> SERIALIZER = DisplaySerializer.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(Shapeless::getInputEntries),
                EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(Shapeless::getOutputEntries),
                Codec.INT.xmap(NetworkRecipeId::new, NetworkRecipeId::index).optionalFieldOf("id").forGetter(Shapeless::recipeDisplayId)
            ).apply(instance, CraftingDisplayShapeless::new)), PacketCodec.tuple(
                EntryIngredient.streamCodec().collect(PacketCodecs.toList()),
                Shapeless::getInputEntries,
                EntryIngredient.streamCodec().collect(PacketCodecs.toList()),
                Shapeless::getOutputEntries,
                PacketCodecs.optional(PacketCodecs.INTEGER.xmap(NetworkRecipeId::new, NetworkRecipeId::index)),
                Shapeless::recipeDisplayId,
                CraftingDisplayShapeless::new
            ), false
        );

        public CraftingDisplayShapeless(ShapelessCraftingRecipeDisplay recipe) {
            super(recipe, Optional.empty());
        }

        public CraftingDisplayShapeless(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<NetworkRecipeId> id) {
            super(inputs, outputs, id);
        }

        @Override
        public CategoryIdentifier<?> getCategoryIdentifier() {
            return ReiCommonPlugin.AUTOMATIC_SHAPELESS;
        }

        @Override
        public DisplaySerializer<? extends Display> getSerializer() {
            return SERIALIZER;
        }
    }
}
