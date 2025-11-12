package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
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

public record SandpaperPolishingDisplay(EntryIngredient input, EntryIngredient output, Optional<Identifier> location) implements Display {
    public static final DisplaySerializer<SandpaperPolishingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().fieldOf("input").forGetter(SandpaperPolishingDisplay::input),
            EntryIngredient.codec().fieldOf("output").forGetter(SandpaperPolishingDisplay::output),
            Identifier.CODEC.optionalFieldOf("location").forGetter(SandpaperPolishingDisplay::location)
        ).apply(instance, SandpaperPolishingDisplay::new)), StreamCodec.composite(
            EntryIngredient.streamCodec(),
            SandpaperPolishingDisplay::input,
            EntryIngredient.streamCodec(),
            SandpaperPolishingDisplay::output,
            ByteBufCodecs.optional(Identifier.STREAM_CODEC),
            SandpaperPolishingDisplay::location,
            SandpaperPolishingDisplay::new
        )
    );

    public SandpaperPolishingDisplay(RecipeHolder<SandPaperPolishingRecipe> entry) {
        this(entry.id().location(), entry.value());
    }

    public SandpaperPolishingDisplay(Identifier id, SandPaperPolishingRecipe recipe) {
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
        return ReiCommonPlugin.SANDPAPER_POLISHING;
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
