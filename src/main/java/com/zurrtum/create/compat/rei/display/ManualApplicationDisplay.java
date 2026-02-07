package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record ManualApplicationDisplay(
    EntryIngredient input, EntryIngredient target, List<ProcessingOutput> outputs, boolean keepHeldItem,
    Optional<Identifier> location
) implements Display {
    public static final DisplaySerializer<ManualApplicationDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().fieldOf("input").forGetter(ManualApplicationDisplay::input),
            EntryIngredient.codec().fieldOf("target").forGetter(ManualApplicationDisplay::target),
            ProcessingOutput.CODEC.listOf().fieldOf("outputs").forGetter(ManualApplicationDisplay::outputs),
            Codec.BOOL.optionalFieldOf("keep_held_item", false).forGetter(ManualApplicationDisplay::keepHeldItem),
            Identifier.CODEC.optionalFieldOf("location").forGetter(ManualApplicationDisplay::location)
        ).apply(instance, ManualApplicationDisplay::new)), PacketCodec.tuple(
            EntryIngredient.streamCodec(),
            ManualApplicationDisplay::input,
            EntryIngredient.streamCodec(),
            ManualApplicationDisplay::target,
            ProcessingOutput.STREAM_CODEC.collect(PacketCodecs.toList()),
            ManualApplicationDisplay::outputs,
            PacketCodecs.BOOLEAN,
            ManualApplicationDisplay::keepHeldItem,
            PacketCodecs.optional(Identifier.PACKET_CODEC),
            ManualApplicationDisplay::location,
            ManualApplicationDisplay::new
        )
    );

    public ManualApplicationDisplay(RecipeEntry<ManualApplicationRecipe> entry) {
        this(entry.id().getValue(), entry.value());
    }

    public ManualApplicationDisplay(Identifier id, ManualApplicationRecipe recipe) {
        this(
            EntryIngredients.ofIngredient(recipe.ingredient()),
            EntryIngredients.ofIngredient(recipe.target()),
            recipe.results(),
            recipe.keepHeldItem(),
            Optional.of(id)
        );
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return List.of(input, target);
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        List<EntryIngredient> list = new ArrayList<>();
        for (ProcessingOutput output : outputs) {
            list.add(EntryIngredients.of(output.create()));
        }
        return list;
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
