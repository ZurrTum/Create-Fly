package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
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

import static com.zurrtum.create.compat.rei.IngredientHelper.*;

public record MixingDisplay(
    List<EntryIngredient> inputs, List<ProcessingOutput> results, List<EntryIngredient> fluidResults,
    HeatCondition heat, Optional<Identifier> location
) implements Display {
    public static final DisplaySerializer<MixingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(MixingDisplay::inputs),
            ProcessingOutput.CODEC.listOf().fieldOf("results").forGetter(MixingDisplay::results),
            EntryIngredient.codec().listOf().fieldOf("fluid_results").forGetter(MixingDisplay::fluidResults),
            HeatCondition.CODEC.fieldOf("heat").forGetter(MixingDisplay::heat),
            Identifier.CODEC.optionalFieldOf("location").forGetter(MixingDisplay::location)
        ).apply(instance, MixingDisplay::new)), PacketCodec.tuple(
            EntryIngredient.streamCodec().collect(PacketCodecs.toList()),
            MixingDisplay::inputs,
            ProcessingOutput.STREAM_CODEC.collect(PacketCodecs.toList()),
            MixingDisplay::results,
            EntryIngredient.streamCodec().collect(PacketCodecs.toList()),
            MixingDisplay::fluidResults,
            HeatCondition.PACKET_CODEC,
            MixingDisplay::heat,
            PacketCodecs.optional(Identifier.PACKET_CODEC),
            MixingDisplay::location,
            MixingDisplay::new
        )
    );

    public MixingDisplay(RecipeEntry<MixingRecipe> entry) {
        this(entry.id().getValue(), entry.value());
    }

    public MixingDisplay(Identifier id, MixingRecipe recipe) {
        this(
            getEntryIngredients(getSizedIngredientStream(recipe.ingredients()), getFluidIngredientStream(recipe.fluidIngredients())),
            recipe.results(),
            getFluidIngredientList(recipe.fluidResults()),
            recipe.heat(),
            Optional.of(id)
        );
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return inputs;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        List<EntryIngredient> list = new ArrayList<>();
        for (ProcessingOutput output : results) {
            list.add(EntryIngredients.of(output.create()));
        }
        list.addAll(fluidResults);
        return list;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ReiCommonPlugin.MIXING;
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
