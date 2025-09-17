package com.zurrtum.create.compat.rei;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.content.kinetics.deployer.ItemApplicationRecipe;
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

public record DeployingDisplay(
    EntryIngredient input, EntryIngredient target, EntryIngredient output, Optional<Identifier> location
) implements Display {
    public static final DisplaySerializer<DeployingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().fieldOf("input").forGetter(DeployingDisplay::input),
            EntryIngredient.codec().fieldOf("target").forGetter(DeployingDisplay::target),
            EntryIngredient.codec().fieldOf("output").forGetter(DeployingDisplay::output),
            Identifier.CODEC.optionalFieldOf("location").forGetter(DeployingDisplay::location)
        ).apply(instance, DeployingDisplay::new)), PacketCodec.tuple(
            EntryIngredient.streamCodec(),
            DeployingDisplay::input,
            EntryIngredient.streamCodec(),
            DeployingDisplay::target,
            EntryIngredient.streamCodec(),
            DeployingDisplay::output,
            PacketCodecs.optional(Identifier.PACKET_CODEC),
            DeployingDisplay::location,
            DeployingDisplay::new
        )
    );

    public DeployingDisplay(RecipeEntry<ItemApplicationRecipe> entry) {
        this(entry.id().getValue(), entry.value());
    }

    public DeployingDisplay(Identifier id, ItemApplicationRecipe recipe) {
        this(
            EntryIngredients.ofIngredient(recipe.ingredient()),
            IngredientHelper.getInputEntryIngredient(recipe.target()),
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
        return ReiCommonPlugin.DEPLOYING;
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
