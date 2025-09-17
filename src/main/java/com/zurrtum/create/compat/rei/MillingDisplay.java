package com.zurrtum.create.compat.rei;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.content.kinetics.millstone.MillingRecipe;
import com.zurrtum.create.content.processing.recipe.ChanceOutput;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record MillingDisplay(EntryIngredient input, List<ChanceOutput> outputs, Optional<Identifier> location) implements Display {
    public static final DisplaySerializer<MillingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().fieldOf("inputs").forGetter(MillingDisplay::input),
            ChanceOutput.CODEC.listOf().fieldOf("output").forGetter(MillingDisplay::outputs),
            Identifier.CODEC.optionalFieldOf("location").forGetter(MillingDisplay::location)
        ).apply(instance, MillingDisplay::new)), PacketCodec.tuple(
            EntryIngredient.streamCodec(),
            MillingDisplay::input,
            ChanceOutput.PACKET_CODEC.collect(PacketCodecs.toList()),
            MillingDisplay::outputs,
            PacketCodecs.optional(Identifier.PACKET_CODEC),
            MillingDisplay::location,
            MillingDisplay::new
        )
    );

    public MillingDisplay(RecipeEntry<MillingRecipe> entry) {
        this(entry.id().getValue(), entry.value());
    }

    public MillingDisplay(Identifier id, MillingRecipe recipe) {
        this(EntryIngredients.ofIngredient(recipe.ingredient()), recipe.results(), Optional.of(id));
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return List.of(input);
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        List<EntryIngredient> list = new ArrayList<>();
        for (ChanceOutput output : outputs) {
            list.add(EntryIngredients.of(output.stack()));
        }
        return list;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ReiCommonPlugin.MILLING;
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
