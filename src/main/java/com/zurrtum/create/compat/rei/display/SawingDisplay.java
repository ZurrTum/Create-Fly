package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.kinetics.saw.CuttingRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import java.util.List;
import java.util.Optional;

public record SawingDisplay(EntryIngredient input, EntryIngredient output, Optional<ResourceLocation> location) implements Display {
    public static final DisplaySerializer<SawingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().fieldOf("input").forGetter(SawingDisplay::input),
            EntryIngredient.codec().fieldOf("output").forGetter(SawingDisplay::output),
            ResourceLocation.CODEC.optionalFieldOf("location").forGetter(SawingDisplay::location)
        ).apply(instance, SawingDisplay::new)), StreamCodec.composite(
            EntryIngredient.streamCodec(),
            SawingDisplay::input,
            EntryIngredient.streamCodec(),
            SawingDisplay::output,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
            SawingDisplay::location,
            SawingDisplay::new
        )
    );

    public SawingDisplay(RecipeHolder<CuttingRecipe> entry) {
        this(entry.id().location(), entry.value());
    }

    public SawingDisplay(ResourceLocation id, CuttingRecipe recipe) {
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
        return ReiCommonPlugin.SAWING;
    }

    @Override
    public Optional<ResourceLocation> getDisplayLocation() {
        return location;
    }

    @Override
    public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
