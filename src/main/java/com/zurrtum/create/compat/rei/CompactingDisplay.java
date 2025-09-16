package com.zurrtum.create.compat.rei;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.content.kinetics.mixer.CompactingRecipe;
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

import static com.zurrtum.create.compat.rei.IngredientHelper.getEntryIngredient;
import static com.zurrtum.create.compat.rei.IngredientHelper.getEntryIngredients;

public record CompactingDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<Identifier> location) implements Display {
    public static final DisplaySerializer<CompactingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(Display::getInputEntries),
            EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(Display::getOutputEntries),
            Identifier.CODEC.optionalFieldOf("location").forGetter(Display::getDisplayLocation)
        ).apply(instance, CompactingDisplay::new)), PacketCodec.tuple(
            EntryIngredient.streamCodec().collect(PacketCodecs.toList()),
            Display::getInputEntries,
            EntryIngredient.streamCodec().collect(PacketCodecs.toList()),
            Display::getOutputEntries,
            PacketCodecs.optional(Identifier.PACKET_CODEC),
            Display::getDisplayLocation,
            CompactingDisplay::new
        )
    );

    public CompactingDisplay(RecipeEntry<CompactingRecipe> entry) {
        this(entry.id().getValue(), entry.value());
    }

    public CompactingDisplay(Identifier id, CompactingRecipe recipe) {
        this(
            getEntryIngredients(getEntryIngredients(recipe.ingredients()), getEntryIngredient(recipe.fluidIngredient())),
            List.of(EntryIngredients.of(recipe.result())),
            Optional.of(id)
        );
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return inputs;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return outputs;
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
