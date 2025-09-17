package com.zurrtum.create.compat.rei;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import dev.architectury.fluid.FluidStack;
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

import static com.zurrtum.create.compat.rei.IngredientHelper.*;

public record MixingDisplay(
    List<EntryIngredient> inputs, EntryIngredient output, HeatCondition heat, Optional<Identifier> location
) implements Display {
    public static final DisplaySerializer<MixingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(MixingDisplay::inputs),
            EntryIngredient.codec().fieldOf("output").forGetter(MixingDisplay::output),
            HeatCondition.CODEC.fieldOf("heat").forGetter(MixingDisplay::heat),
            Identifier.CODEC.optionalFieldOf("location").forGetter(MixingDisplay::location)
        ).apply(instance, MixingDisplay::new)), PacketCodec.tuple(
            EntryIngredient.streamCodec().collect(PacketCodecs.toList()),
            MixingDisplay::inputs,
            EntryIngredient.streamCodec(),
            MixingDisplay::output,
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
            recipe.result().isEmpty() ? EntryIngredients.of(FluidStack.create(
                recipe.fluidResult().getFluid(),
                recipe.fluidResult().getAmount()
            )) : EntryIngredients.of(recipe.result()),
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
        return List.of(output);
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
