package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.kinetics.fan.processing.SplashingRecipe;
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

public record FanWashingDisplay(EntryIngredient input, List<ProcessingOutput> outputs,
                                Optional<Identifier> location) implements Display {
    public static final DisplaySerializer<FanWashingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().fieldOf("input").forGetter(FanWashingDisplay::input),
            ProcessingOutput.CODEC.listOf().fieldOf("outputs").forGetter(FanWashingDisplay::outputs),
            Identifier.CODEC.optionalFieldOf("location").forGetter(FanWashingDisplay::location)
        ).apply(instance, FanWashingDisplay::new)), PacketCodec.tuple(
            EntryIngredient.streamCodec(),
            FanWashingDisplay::input,
            ProcessingOutput.STREAM_CODEC.collect(PacketCodecs.toList()),
            FanWashingDisplay::outputs,
            PacketCodecs.optional(Identifier.PACKET_CODEC),
            FanWashingDisplay::location,
            FanWashingDisplay::new
        )
    );

    public FanWashingDisplay(RecipeEntry<SplashingRecipe> entry) {
        this(entry.id().getValue(), entry.value());
    }

    public FanWashingDisplay(Identifier id, SplashingRecipe recipe) {
        this(EntryIngredients.ofIngredient(recipe.ingredient()), recipe.results(), Optional.of(id));
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return List.of(input);
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
        return ReiCommonPlugin.FAN_WASHING;
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
