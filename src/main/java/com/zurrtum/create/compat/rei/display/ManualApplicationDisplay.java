package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.kinetics.deployer.ManualApplicationRecipe;
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

public record ManualApplicationDisplay(
    EntryIngredient input, EntryIngredient target, EntryIngredient output, Optional<Identifier> location
) implements Display {
    public static final DisplaySerializer<ManualApplicationDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().fieldOf("input").forGetter(ManualApplicationDisplay::input),
            EntryIngredient.codec().fieldOf("target").forGetter(ManualApplicationDisplay::target),
            EntryIngredient.codec().fieldOf("output").forGetter(ManualApplicationDisplay::output),
            Identifier.CODEC.optionalFieldOf("location").forGetter(ManualApplicationDisplay::location)
        ).apply(instance, ManualApplicationDisplay::new)), StreamCodec.composite(
            EntryIngredient.streamCodec(),
            ManualApplicationDisplay::input,
            EntryIngredient.streamCodec(),
            ManualApplicationDisplay::target,
            EntryIngredient.streamCodec(),
            ManualApplicationDisplay::output,
            ByteBufCodecs.optional(Identifier.STREAM_CODEC),
            ManualApplicationDisplay::location,
            ManualApplicationDisplay::new
        )
    );

    public ManualApplicationDisplay(RecipeHolder<ManualApplicationRecipe> entry) {
        this(entry.id().location(), entry.value());
    }

    public ManualApplicationDisplay(Identifier id, ManualApplicationRecipe recipe) {
        this(
            EntryIngredients.ofIngredient(recipe.ingredient()),
            EntryIngredients.ofIngredient(recipe.target()),
            EntryIngredients.of(recipe.result()),
            Optional.of(id)
        );
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return List.of(input, target);
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return List.of(output);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ReiCommonPlugin.ITEM_APPLICATION;
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
