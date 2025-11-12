package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.compat.rei.IngredientHelper;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.kinetics.press.PressingRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;
import java.util.Optional;

public record PressingDisplay(EntryIngredient input, EntryIngredient output, Optional<Identifier> location) implements Display {
    public static final DisplaySerializer<PressingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().fieldOf("inputs").forGetter(PressingDisplay::input),
            EntryIngredient.codec().fieldOf("output").forGetter(PressingDisplay::output),
            Identifier.CODEC.optionalFieldOf("location").forGetter(PressingDisplay::location)
        ).apply(instance, PressingDisplay::new)), StreamCodec.composite(
            EntryIngredient.streamCodec(),
            PressingDisplay::input,
            EntryIngredient.streamCodec(),
            PressingDisplay::output,
            ByteBufCodecs.optional(Identifier.STREAM_CODEC),
            PressingDisplay::location,
            PressingDisplay::new
        )
    );

    public PressingDisplay(RecipeHolder<PressingRecipe> entry) {
        this(entry.id().location(), entry.value());
    }

    public PressingDisplay(Identifier id, PressingRecipe recipe) {
        this(IngredientHelper.getInputEntryIngredient(recipe.ingredient()), EntryIngredients.of(recipe.result()), Optional.of(id));
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
        return ReiCommonPlugin.PRESSING;
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
