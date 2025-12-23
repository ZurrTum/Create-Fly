package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.kinetics.press.MechanicalPressBlockEntity;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.InputIngredient;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.client.displays.ClientsidedCraftingDisplay;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCustomShapelessDisplay;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface AutoMixingDisplay {
    @SuppressWarnings("unchecked")
    static Display of(RecipeHolder<?> entry) {
        Recipe<?> recipe = entry.value();
        if (MechanicalPressBlockEntity.canCompress(recipe) || AllRecipeTypes.shouldIgnoreInAutomation(entry)) {
            return null;
        }
        if (recipe instanceof ShapelessRecipe shapelessRecipe) {
            if (shapelessRecipe.ingredients.size() > 1) {
                return new ShapelessDisplay((RecipeHolder<ShapelessRecipe>) entry);
            }
        } else if (!recipe.isSpecial()) {
            for (RecipeDisplay d : recipe.display()) {
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
            ).apply(instance, ShapelessDisplay::new)), StreamCodec.composite(
                EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                DefaultCraftingDisplay::getInputEntries,
                EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                DefaultCraftingDisplay::getOutputEntries,
                ByteBufCodecs.optional(Identifier.STREAM_CODEC),
                DefaultCraftingDisplay::getDisplayLocation,
                ShapelessDisplay::new
            )
        );

        public ShapelessDisplay(List<EntryIngredient> input, List<EntryIngredient> output, Optional<Identifier> location) {
            super(input, output, location);
        }

        public ShapelessDisplay(RecipeHolder<ShapelessRecipe> recipe) {
            super(
                CollectionUtils.map(recipe.value().placementInfo().ingredients(), EntryIngredients::ofIngredient),
                List.of(EntryIngredients.of(recipe.value().result)),
                Optional.of(recipe.id().identifier())
            );
        }

        @Override
        public List<InputIngredient<EntryStack<?>>> getInputIngredients(@Nullable AbstractContainerMenu menu, @Nullable Player player) {
            return CollectionUtils.mapIndexed(getInputEntries(), InputIngredient::of);
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
                Codec.INT.xmap(RecipeDisplayId::new, RecipeDisplayId::index).optionalFieldOf("id").forGetter(Shapeless::recipeDisplayId)
            ).apply(instance, CraftingDisplayShapeless::new)), StreamCodec.composite(
                EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                Shapeless::getInputEntries,
                EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                Shapeless::getOutputEntries,
                ByteBufCodecs.optional(ByteBufCodecs.INT.map(RecipeDisplayId::new, RecipeDisplayId::index)),
                Shapeless::recipeDisplayId,
                CraftingDisplayShapeless::new
            ), false
        );

        public CraftingDisplayShapeless(ShapelessCraftingRecipeDisplay recipe) {
            super(recipe, Optional.empty());
        }

        public CraftingDisplayShapeless(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<RecipeDisplayId> id) {
            super(inputs, outputs, id);
        }

        @Override
        public List<InputIngredient<EntryStack<?>>> getInputIngredients(@Nullable AbstractContainerMenu menu, @Nullable Player player) {
            return CollectionUtils.mapIndexed(getInputEntries(), InputIngredient::of);
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
