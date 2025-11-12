package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.compat.rei.IngredientHelper;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.kinetics.mixer.PotionRecipe;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
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

public record PotionDisplay(EntryIngredient input, FluidIngredient fluid, FluidStack output, Optional<ResourceLocation> location) implements Display {
    public static final DisplaySerializer<PotionDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().fieldOf("input").forGetter(PotionDisplay::input),
            FluidIngredient.CODEC.fieldOf("fluid").forGetter(PotionDisplay::fluid),
            FluidStack.CODEC.fieldOf("output").forGetter(PotionDisplay::output),
            ResourceLocation.CODEC.optionalFieldOf("location").forGetter(PotionDisplay::location)
        ).apply(instance, PotionDisplay::new)), StreamCodec.composite(
            EntryIngredient.streamCodec(),
            PotionDisplay::input,
            FluidIngredient.PACKET_CODEC,
            PotionDisplay::fluid,
            FluidStack.PACKET_CODEC,
            PotionDisplay::output,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
            PotionDisplay::location,
            PotionDisplay::new
        )
    );

    public PotionDisplay(RecipeHolder<PotionRecipe> entry) {
        this(entry.id().location(), entry.value());
    }

    public PotionDisplay(ResourceLocation id, PotionRecipe recipe) {
        this(EntryIngredients.ofIngredient(recipe.ingredient()), recipe.fluidIngredient(), recipe.result(), Optional.of(id));
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return List.of(input, IngredientHelper.createEntryIngredient(fluid));
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return List.of(IngredientHelper.createEntryIngredient(output));
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ReiCommonPlugin.AUTOMATIC_BREWING;
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
