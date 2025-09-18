package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import java.util.List;
import java.util.Optional;

public record MechanicalCraftingDisplay(
    int width, int height, List<Optional<Ingredient>> inputs, EntryIngredient output, Optional<Identifier> location
) implements Display {
    public static final DisplaySerializer<MechanicalCraftingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("width").forGetter(MechanicalCraftingDisplay::width),
            Codec.INT.fieldOf("height").forGetter(MechanicalCraftingDisplay::height),
            Codecs.optional(Ingredient.CODEC).listOf().fieldOf("inputs").forGetter(MechanicalCraftingDisplay::inputs),
            EntryIngredient.codec().fieldOf("output").forGetter(MechanicalCraftingDisplay::output),
            Identifier.CODEC.optionalFieldOf("location").forGetter(MechanicalCraftingDisplay::location)
        ).apply(instance, MechanicalCraftingDisplay::new)), PacketCodec.tuple(
            PacketCodecs.VAR_INT,
            MechanicalCraftingDisplay::width,
            PacketCodecs.VAR_INT,
            MechanicalCraftingDisplay::height,
            Ingredient.OPTIONAL_PACKET_CODEC.collect(PacketCodecs.toList()),
            MechanicalCraftingDisplay::inputs,
            EntryIngredient.streamCodec(),
            MechanicalCraftingDisplay::output,
            Identifier.PACKET_CODEC.collect(PacketCodecs::optional),
            MechanicalCraftingDisplay::location,
            MechanicalCraftingDisplay::new
        )
    );

    public MechanicalCraftingDisplay(RecipeEntry<MechanicalCraftingRecipe> entry) {
        this(entry.id().getValue(), entry.value());
    }

    public MechanicalCraftingDisplay(Identifier id, MechanicalCraftingRecipe recipe) {
        this(recipe.raw().getWidth(), recipe.raw().getHeight(), recipe.raw().getIngredients(), EntryIngredients.of(recipe.result()), Optional.of(id));
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return inputs.stream().filter(Optional::isPresent).map(Optional::get).map(EntryIngredients::ofIngredient).toList();
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return List.of(output);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ReiCommonPlugin.MECHANICAL_CRAFTING;
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
