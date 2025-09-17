package com.zurrtum.create.compat.rei;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.content.kinetics.saw.CuttingRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public record SawingDisplay(EntryIngredient input, EntryIngredient output, Optional<Identifier> location) implements Display {
    public static final DisplaySerializer<SawingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().fieldOf("input").forGetter(SawingDisplay::input),
            EntryIngredient.codec().fieldOf("output").forGetter(SawingDisplay::output),
            Identifier.CODEC.optionalFieldOf("location").forGetter(SawingDisplay::location)
        ).apply(instance, SawingDisplay::new)), PacketCodec.tuple(
            EntryIngredient.streamCodec(),
            SawingDisplay::input,
            EntryIngredient.streamCodec(),
            SawingDisplay::output,
            PacketCodecs.optional(Identifier.PACKET_CODEC),
            SawingDisplay::location,
            SawingDisplay::new
        )
    );

    public SawingDisplay(RecipeEntry<CuttingRecipe> entry) {
        this(entry.id().getValue(), entry.value());
    }

    public SawingDisplay(Identifier id, CuttingRecipe recipe) {
        this(EntryIngredients.ofIngredient(recipe.ingredient()), EntryIngredients.of(recipe.result()), Optional.of(id));
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
        return ReiCommonPlugin.SAWING;
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
