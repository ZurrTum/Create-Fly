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
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCustomShapedDisplay;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCustomShapelessDisplay;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface AutoCompactingDisplay {
    @SuppressWarnings("unchecked")
    static Display of(RecipeHolder<?> entry) {
        Recipe<?> recipe = entry.value();
        if (!MechanicalPressBlockEntity.canCompress(recipe) || AllRecipeTypes.shouldIgnoreInAutomation(entry)) {
            return null;
        }
        if (recipe instanceof ShapelessRecipe) {
            return new ShapelessDisplay((RecipeHolder<ShapelessRecipe>) entry);
        } else if (recipe instanceof ShapedRecipe) {
            return new ShapedDisplay((RecipeHolder<ShapedRecipe>) entry);
        } else if (!recipe.isSpecial()) {
            for (RecipeDisplay d : recipe.display()) {
                if (d instanceof ShapedCraftingRecipeDisplay display) {
                    return new CraftingDisplayShaped(display);
                } else if (d instanceof ShapelessCraftingRecipeDisplay display) {
                    return new CraftingDisplayShapeless(display);
                }
            }
        }
        return null;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    class ShapelessDisplay extends DefaultCustomShapelessDisplay implements AutoCompactingDisplay {
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
            return ReiCommonPlugin.AUTOMATIC_PACKING;
        }

        @Override
        public DisplaySerializer<? extends Display> getSerializer() {
            return SERIALIZER;
        }
    }

    class ShapedDisplay extends DefaultCustomShapedDisplay implements AutoCompactingDisplay {
        public static final DisplaySerializer<ShapedDisplay> SERIALIZER = DisplaySerializer.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(DefaultCraftingDisplay::getInputEntries),
                EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(DefaultCraftingDisplay::getOutputEntries),
                Identifier.CODEC.optionalFieldOf("location").forGetter(DefaultCraftingDisplay::getDisplayLocation),
                Codec.INT.fieldOf("width").forGetter(DefaultCraftingDisplay::getWidth),
                Codec.INT.fieldOf("height").forGetter(DefaultCraftingDisplay::getHeight)
            ).apply(instance, ShapedDisplay::new)), StreamCodec.composite(
                EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                DefaultCraftingDisplay::getInputEntries,
                EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                DefaultCraftingDisplay::getOutputEntries,
                ByteBufCodecs.optional(Identifier.STREAM_CODEC),
                DefaultCraftingDisplay::getDisplayLocation,
                ByteBufCodecs.INT,
                DefaultCraftingDisplay::getWidth,
                ByteBufCodecs.INT,
                DefaultCraftingDisplay::getHeight,
                ShapedDisplay::new
            )
        );

        public ShapedDisplay(RecipeHolder<ShapedRecipe> recipe) {
            super(
                CollectionUtils.map(recipe.value().getIngredients(), opt -> opt.map(EntryIngredients::ofIngredient).orElse(EntryIngredient.empty())),
                List.of(EntryIngredients.of(recipe.value().result)),
                Optional.of(recipe.id().identifier()),
                recipe.value().getWidth(),
                recipe.value().getHeight()
            );
        }

        public ShapedDisplay(List<EntryIngredient> input, List<EntryIngredient> output, Optional<Identifier> location, int width, int height) {
            super(input, output, location, width, height);
        }

        @Override
        public List<InputIngredient<EntryStack<?>>> getInputIngredients(@Nullable AbstractContainerMenu menu, @Nullable Player player) {
            return CollectionUtils.mapIndexed(getInputEntries(), InputIngredient::of);
        }

        @Override
        public CategoryIdentifier<?> getCategoryIdentifier() {
            return ReiCommonPlugin.AUTOMATIC_PACKING;
        }

        @Override
        public DisplaySerializer<? extends Display> getSerializer() {
            return SERIALIZER;
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    class CraftingDisplayShaped extends ClientsidedCraftingDisplay.Shaped implements AutoCompactingDisplay {
        public static final DisplaySerializer<CraftingDisplayShaped> SERIALIZER = DisplaySerializer.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(Shaped::getInputEntries),
                EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(Shaped::getOutputEntries),
                Codec.INT.xmap(RecipeDisplayId::new, RecipeDisplayId::index).optionalFieldOf("id").forGetter(Shaped::recipeDisplayId),
                Codec.INT.fieldOf("width").forGetter(Shaped::getWidth),
                Codec.INT.fieldOf("height").forGetter(Shaped::getHeight)
            ).apply(instance, CraftingDisplayShaped::new)), StreamCodec.composite(
                EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                Shaped::getInputEntries,
                EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                Shaped::getOutputEntries,
                ByteBufCodecs.optional(ByteBufCodecs.INT.map(RecipeDisplayId::new, RecipeDisplayId::index)),
                Shaped::recipeDisplayId,
                ByteBufCodecs.INT,
                Shaped::getWidth,
                ByteBufCodecs.INT,
                Shaped::getHeight,
                CraftingDisplayShaped::new
            ), false
        );

        public CraftingDisplayShaped(ShapedCraftingRecipeDisplay recipe) {
            super(recipe, Optional.empty());
        }

        public CraftingDisplayShaped(
            List<EntryIngredient> inputs,
            List<EntryIngredient> outputs,
            Optional<RecipeDisplayId> id,
            int width,
            int height
        ) {
            super(inputs, outputs, id, width, height);
        }

        @Override
        public List<InputIngredient<EntryStack<?>>> getInputIngredients(@Nullable AbstractContainerMenu menu, @Nullable Player player) {
            return CollectionUtils.mapIndexed(getInputEntries(), InputIngredient::of);
        }

        @Override
        public CategoryIdentifier<?> getCategoryIdentifier() {
            return ReiCommonPlugin.AUTOMATIC_PACKING;
        }

        @Override
        public DisplaySerializer<? extends Display> getSerializer() {
            return SERIALIZER;
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    class CraftingDisplayShapeless extends ClientsidedCraftingDisplay.Shapeless implements AutoCompactingDisplay {
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
            return ReiCommonPlugin.AUTOMATIC_PACKING;
        }

        @Override
        public DisplaySerializer<? extends Display> getSerializer() {
            return SERIALIZER;
        }
    }
}
