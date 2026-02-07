package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.compat.rei.IngredientHelper;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.kinetics.mixer.CompactingRecipe;
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

import static com.zurrtum.create.compat.rei.IngredientHelper.getEntryIngredients;
import static com.zurrtum.create.compat.rei.IngredientHelper.getFluidIngredientStream;

public record CompactingDisplay(List<EntryIngredient> inputs, List<ProcessingOutput> outputs, HeatCondition heat,
                                Optional<Identifier> location) implements Display {
    public static final DisplaySerializer<CompactingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(CompactingDisplay::inputs),
            ProcessingOutput.CODEC.listOf().fieldOf("outputs").forGetter(CompactingDisplay::outputs),
            HeatCondition.CODEC.fieldOf("heat").forGetter(CompactingDisplay::heat),
            Identifier.CODEC.optionalFieldOf("location").forGetter(CompactingDisplay::location)
        ).apply(instance, CompactingDisplay::new)), PacketCodec.tuple(
            EntryIngredient.streamCodec().collect(PacketCodecs.toList()),
            CompactingDisplay::inputs,
            ProcessingOutput.STREAM_CODEC.collect(PacketCodecs.toList()),
            CompactingDisplay::outputs,
            HeatCondition.PACKET_CODEC,
            CompactingDisplay::heat,
            PacketCodecs.optional(Identifier.PACKET_CODEC),
            CompactingDisplay::location,
            CompactingDisplay::new
        )
    );

    public CompactingDisplay(RecipeEntry<CompactingRecipe> entry) {
        this(entry.id().getValue(), entry.value());
    }

    public CompactingDisplay(Identifier id, CompactingRecipe recipe) {
        this(
            getEntryIngredients(IngredientHelper.getSizedIngredientStream(recipe.ingredients()), getFluidIngredientStream(recipe.fluidIngredients())),
            recipe.results(),
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
        for (ProcessingOutput output : outputs) {
            list.add(EntryIngredients.of(output.create()));
        }
        return list;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ReiCommonPlugin.PACKING;
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
