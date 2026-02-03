package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.kinetics.fan.processing.HauntingRecipe;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record FanHauntingDisplay(EntryIngredient input, List<ProcessingOutput> outputs, Optional<Identifier> location) implements Display {
    public static final DisplaySerializer<FanHauntingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().fieldOf("input").forGetter(FanHauntingDisplay::input),
            ProcessingOutput.CODEC.listOf().fieldOf("outputs").forGetter(FanHauntingDisplay::outputs),
            Identifier.CODEC.optionalFieldOf("location").forGetter(FanHauntingDisplay::location)
        ).apply(instance, FanHauntingDisplay::new)), StreamCodec.composite(
            EntryIngredient.streamCodec(),
            FanHauntingDisplay::input,
            ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()),
            FanHauntingDisplay::outputs,
            ByteBufCodecs.optional(Identifier.STREAM_CODEC),
            FanHauntingDisplay::location,
            FanHauntingDisplay::new
        )
    );

    public FanHauntingDisplay(RecipeHolder<HauntingRecipe> entry) {
        this(entry.id().identifier(), entry.value());
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
        for (ProcessingOutput output : outputs) {
            list.add(EntryIngredients.of(output.create()));
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
