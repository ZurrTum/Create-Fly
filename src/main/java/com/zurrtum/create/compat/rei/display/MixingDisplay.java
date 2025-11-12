package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import dev.architectury.fluid.FluidStack;
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
        ).apply(instance, MixingDisplay::new)), StreamCodec.composite(
            EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
            MixingDisplay::inputs,
            EntryIngredient.streamCodec(),
            MixingDisplay::output,
            HeatCondition.PACKET_CODEC,
            MixingDisplay::heat,
            ByteBufCodecs.optional(Identifier.STREAM_CODEC),
            MixingDisplay::location,
            MixingDisplay::new
        )
    );

    public MixingDisplay(RecipeHolder<MixingRecipe> entry) {
        this(entry.id().location(), entry.value());
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
