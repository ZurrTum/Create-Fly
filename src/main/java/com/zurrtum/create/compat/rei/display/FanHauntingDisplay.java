package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.kinetics.fan.processing.HauntingRecipe;
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

public record FanHauntingDisplay(EntryIngredient input, List<ChanceOutput> outputs, Optional<Identifier> location) implements Display {
    public static final DisplaySerializer<FanHauntingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().fieldOf("input").forGetter(FanHauntingDisplay::input),
            ChanceOutput.CODEC.listOf().fieldOf("outputs").forGetter(FanHauntingDisplay::outputs),
            Identifier.CODEC.optionalFieldOf("location").forGetter(FanHauntingDisplay::location)
        ).apply(instance, FanHauntingDisplay::new)), PacketCodec.tuple(
            EntryIngredient.streamCodec(),
            FanHauntingDisplay::input,
            ChanceOutput.PACKET_CODEC.collect(PacketCodecs.toList()),
            FanHauntingDisplay::outputs,
            PacketCodecs.optional(Identifier.PACKET_CODEC),
            FanHauntingDisplay::location,
            FanHauntingDisplay::new
        )
    );

    public FanHauntingDisplay(RecipeEntry<HauntingRecipe> entry) {
        this(entry.id().getValue(), entry.value());
    }

    public FanHauntingDisplay(Identifier id, HauntingRecipe recipe) {
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
        return ReiCommonPlugin.FAN_HAUNTING;
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
