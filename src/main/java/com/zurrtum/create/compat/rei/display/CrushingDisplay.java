package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.kinetics.crusher.CrushingRecipe;
import com.zurrtum.create.content.kinetics.millstone.MillingRecipe;
import com.zurrtum.create.content.processing.recipe.ChanceOutput;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record CrushingDisplay(EntryIngredient input, List<ChanceOutput> outputs, Optional<Identifier> location) implements Display {
    public static final DisplaySerializer<CrushingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().fieldOf("input").forGetter(CrushingDisplay::input),
            ChanceOutput.CODEC.listOf().fieldOf("outputs").forGetter(CrushingDisplay::outputs),
            Identifier.CODEC.optionalFieldOf("location").forGetter(CrushingDisplay::location)
        ).apply(instance, CrushingDisplay::new)), PacketCodec.tuple(
            EntryIngredient.streamCodec(),
            CrushingDisplay::input,
            ChanceOutput.PACKET_CODEC.collect(PacketCodecs.toList()),
            CrushingDisplay::outputs,
            PacketCodecs.optional(Identifier.PACKET_CODEC),
            CrushingDisplay::location,
            CrushingDisplay::new
        )
    );

    public static CrushingDisplay of(RecipeEntry<?> entry) {
        Identifier id = entry.id().getValue();
        Recipe<?> recipe = entry.value();
        if (recipe instanceof CrushingRecipe crushingRecipe) {
            return new CrushingDisplay(id, crushingRecipe);
        } else if (recipe instanceof MillingRecipe millingRecipe) {
            return new CrushingDisplay(id, millingRecipe);
        }
        return null;
    }

    public CrushingDisplay(Identifier id, CrushingRecipe recipe) {
        this(EntryIngredients.ofIngredient(recipe.ingredient()), recipe.results(), Optional.of(id));
    }

    public CrushingDisplay(Identifier id, MillingRecipe recipe) {
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
        return ReiCommonPlugin.CRUSHING;
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
