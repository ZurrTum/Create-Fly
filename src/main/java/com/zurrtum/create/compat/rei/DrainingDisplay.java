package com.zurrtum.create.compat.rei;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.content.fluids.transfer.EmptyingRecipe;
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

public record DrainingDisplay(
    EntryIngredient input, EntryIngredient output, EntryIngredient result, Optional<Identifier> location
) implements Display {
    public static final DisplaySerializer<DrainingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().fieldOf("input").forGetter(DrainingDisplay::input),
            EntryIngredient.codec().fieldOf("output").forGetter(DrainingDisplay::output),
            EntryIngredient.codec().fieldOf("result").forGetter(DrainingDisplay::result),
            Identifier.CODEC.optionalFieldOf("location").forGetter(DrainingDisplay::location)
        ).apply(instance, DrainingDisplay::new)), PacketCodec.tuple(
            EntryIngredient.streamCodec(),
            DrainingDisplay::input,
            EntryIngredient.streamCodec(),
            DrainingDisplay::output,
            EntryIngredient.streamCodec(),
            DrainingDisplay::result,
            PacketCodecs.optional(Identifier.PACKET_CODEC),
            DrainingDisplay::location,
            DrainingDisplay::new
        )
    );

    public DrainingDisplay(RecipeEntry<EmptyingRecipe> entry) {
        this(entry.id().getValue(), entry.value());
    }

    public DrainingDisplay(Identifier id, EmptyingRecipe recipe) {
        this(
            EntryIngredients.ofIngredient(recipe.ingredient()),
            IngredientHelper.createEntryIngredient(recipe.fluidResult()),
            EntryIngredients.of(recipe.result()),
            Optional.of(id)
        );
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return List.of(input);
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return List.of(output, result);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ReiCommonPlugin.DRAINING;
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
