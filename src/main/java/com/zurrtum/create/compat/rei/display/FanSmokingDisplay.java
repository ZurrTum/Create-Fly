package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmokingRecipe;

import java.util.List;
import java.util.Optional;

public record FanSmokingDisplay(EntryIngredient input, EntryIngredient output, Optional<Identifier> location) implements Display {
    public static final DisplaySerializer<FanSmokingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().fieldOf("input").forGetter(FanSmokingDisplay::input),
            EntryIngredient.codec().fieldOf("output").forGetter(FanSmokingDisplay::output),
            Identifier.CODEC.optionalFieldOf("location").forGetter(FanSmokingDisplay::location)
        ).apply(instance, FanSmokingDisplay::new)), StreamCodec.composite(
            EntryIngredient.streamCodec(),
            FanSmokingDisplay::input,
            EntryIngredient.streamCodec(),
            FanSmokingDisplay::output,
            ByteBufCodecs.optional(Identifier.STREAM_CODEC),
            FanSmokingDisplay::location,
            FanSmokingDisplay::new
        )
    );

    public static Display of(RecipeHolder<SmokingRecipe> entry) {
        if (!AllRecipeTypes.CAN_BE_AUTOMATED.test(entry)) {
            return null;
        }
        SmokingRecipe recipe = entry.value();
        return new FanSmokingDisplay(
            EntryIngredients.ofIngredient(recipe.input()),
            EntryIngredients.of(recipe.result()),
            Optional.of(entry.id().location())
        );
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return List.of(input);
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return List.of(output);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ReiCommonPlugin.FAN_SMOKING;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return location;
    }

    @Override
    public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
