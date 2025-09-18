package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.compat.rei.IngredientHelper;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
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

public record SpoutFillingDisplay(
    EntryIngredient input, EntryIngredient fluid, EntryIngredient output, Optional<Identifier> location
) implements Display {
    public static final DisplaySerializer<SpoutFillingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().fieldOf("input").forGetter(SpoutFillingDisplay::input),
            EntryIngredient.codec().fieldOf("fluid").forGetter(SpoutFillingDisplay::fluid),
            EntryIngredient.codec().fieldOf("output").forGetter(SpoutFillingDisplay::output),
            Identifier.CODEC.optionalFieldOf("location").forGetter(SpoutFillingDisplay::location)
        ).apply(instance, SpoutFillingDisplay::new)), PacketCodec.tuple(
            EntryIngredient.streamCodec(),
            SpoutFillingDisplay::input,
            EntryIngredient.streamCodec(),
            SpoutFillingDisplay::fluid,
            EntryIngredient.streamCodec(),
            SpoutFillingDisplay::output,
            PacketCodecs.optional(Identifier.PACKET_CODEC),
            SpoutFillingDisplay::location,
            SpoutFillingDisplay::new
        )
    );

    public SpoutFillingDisplay(RecipeEntry<FillingRecipe> entry) {
        this(entry.id().getValue(), entry.value());
    }

    public SpoutFillingDisplay(Identifier id, FillingRecipe recipe) {
        this(
            EntryIngredients.ofIngredient(recipe.ingredient()),
            IngredientHelper.createEntryIngredient(recipe.fluidIngredient()),
            EntryIngredients.of(recipe.result()),
            Optional.of(id)
        );
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return List.of(input, fluid);
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return List.of(output);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ReiCommonPlugin.SPOUT_FILLING;
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
