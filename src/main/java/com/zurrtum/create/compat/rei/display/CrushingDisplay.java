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
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record CrushingDisplay(EntryIngredient input, List<ChanceOutput> outputs, Optional<ResourceLocation> location) implements Display {
    public static final DisplaySerializer<CrushingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().fieldOf("input").forGetter(CrushingDisplay::input),
            ChanceOutput.CODEC.listOf().fieldOf("outputs").forGetter(CrushingDisplay::outputs),
            ResourceLocation.CODEC.optionalFieldOf("location").forGetter(CrushingDisplay::location)
        ).apply(instance, CrushingDisplay::new)), StreamCodec.composite(
            EntryIngredient.streamCodec(),
            CrushingDisplay::input,
            ChanceOutput.PACKET_CODEC.apply(ByteBufCodecs.list()),
            CrushingDisplay::outputs,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
            CrushingDisplay::location,
            CrushingDisplay::new
        )
    );

    public static CrushingDisplay of(RecipeHolder<?> entry) {
        ResourceLocation id = entry.id().location();
        Recipe<?> recipe = entry.value();
        if (recipe instanceof CrushingRecipe crushingRecipe) {
            return new CrushingDisplay(id, crushingRecipe);
        } else if (recipe instanceof MillingRecipe millingRecipe) {
            return new CrushingDisplay(id, millingRecipe);
        }
        return null;
    }

    public CrushingDisplay(ResourceLocation id, CrushingRecipe recipe) {
        this(EntryIngredients.ofIngredient(recipe.ingredient()), recipe.results(), Optional.of(id));
    }

    public CrushingDisplay(ResourceLocation id, MillingRecipe recipe) {
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
    public Optional<ResourceLocation> getDisplayLocation() {
        return location;
    }

    @Override
    public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
