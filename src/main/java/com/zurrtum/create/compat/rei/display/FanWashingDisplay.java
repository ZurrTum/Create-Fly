package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.kinetics.fan.processing.SplashingRecipe;
import com.zurrtum.create.content.processing.recipe.ChanceOutput;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record FanWashingDisplay(EntryIngredient input, List<ChanceOutput> outputs, Optional<ResourceLocation> location) implements Display {
    public static final DisplaySerializer<FanWashingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().fieldOf("input").forGetter(FanWashingDisplay::input),
            ChanceOutput.CODEC.listOf().fieldOf("outputs").forGetter(FanWashingDisplay::outputs),
            ResourceLocation.CODEC.optionalFieldOf("location").forGetter(FanWashingDisplay::location)
        ).apply(instance, FanWashingDisplay::new)), StreamCodec.composite(
            EntryIngredient.streamCodec(),
            FanWashingDisplay::input,
            ChanceOutput.PACKET_CODEC.apply(ByteBufCodecs.list()),
            FanWashingDisplay::outputs,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
            FanWashingDisplay::location,
            FanWashingDisplay::new
        )
    );

    public FanWashingDisplay(RecipeHolder<SplashingRecipe> entry) {
        this(entry.id().location(), entry.value());
    }

    public FanWashingDisplay(ResourceLocation id, SplashingRecipe recipe) {
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
        return ReiCommonPlugin.FAN_WASHING;
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
