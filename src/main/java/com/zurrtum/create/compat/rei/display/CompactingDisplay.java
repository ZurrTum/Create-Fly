package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.compat.rei.IngredientHelper;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.kinetics.mixer.CompactingRecipe;
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

import static com.zurrtum.create.compat.rei.IngredientHelper.getEntryIngredients;
import static com.zurrtum.create.compat.rei.IngredientHelper.getFluidIngredientStream;

public record CompactingDisplay(List<EntryIngredient> inputs, EntryIngredient output, Optional<Identifier> location) implements Display {
    public static final DisplaySerializer<CompactingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(CompactingDisplay::inputs),
            EntryIngredient.codec().fieldOf("output").forGetter(CompactingDisplay::output),
            Identifier.CODEC.optionalFieldOf("location").forGetter(CompactingDisplay::location)
        ).apply(instance, CompactingDisplay::new)), StreamCodec.composite(
            EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
            CompactingDisplay::inputs,
            EntryIngredient.streamCodec(),
            CompactingDisplay::output,
            ByteBufCodecs.optional(Identifier.STREAM_CODEC),
            CompactingDisplay::location,
            CompactingDisplay::new
        )
    );

    public CompactingDisplay(RecipeHolder<CompactingRecipe> entry) {
        this(entry.id().identifier(), entry.value());
    }

    public CompactingDisplay(Identifier id, CompactingRecipe recipe) {
        this(
            getEntryIngredients(IngredientHelper.getSizedIngredientStream(recipe.ingredients()), getFluidIngredientStream(recipe.fluidIngredient())),
            EntryIngredients.of(recipe.result()),
            Optional.of(id)
        );
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return inputs;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return List.of(output);
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
