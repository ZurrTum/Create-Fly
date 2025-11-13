package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.compat.rei.IngredientHelper;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.zurrtum.create.content.kinetics.deployer.ItemApplicationRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

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
        ).apply(instance, DeployingDisplay::new)), StreamCodec.composite(
            EntryIngredient.streamCodec(),
            DeployingDisplay::input,
            EntryIngredient.streamCodec(),
            DeployingDisplay::target,
            EntryIngredient.streamCodec(),
            DeployingDisplay::output,
            ByteBufCodecs.optional(Identifier.STREAM_CODEC),
            DeployingDisplay::location,
            DeployingDisplay::new
        )
    );

    public static DeployingDisplay of(RecipeHolder<?> entry) {
        if (!AllRecipeTypes.CAN_BE_AUTOMATED.test(entry)) {
            return null;
        }
        Identifier id = entry.id().identifier();
        Recipe<?> recipe = entry.value();
        if (recipe instanceof ItemApplicationRecipe itemApplicationRecipe) {
            return new DeployingDisplay(id, itemApplicationRecipe);
        } else if (recipe instanceof SandPaperPolishingRecipe sandPaperPolishingRecipe) {
            return new DeployingDisplay(id, sandPaperPolishingRecipe);
        }
        return null;
    }

    public DeployingDisplay(Identifier id, ItemApplicationRecipe recipe) {
        this(
            EntryIngredients.ofIngredient(recipe.ingredient()),
            IngredientHelper.getInputEntryIngredient(recipe.target()),
            EntryIngredients.of(recipe.result()),
            Optional.of(id)
        );
    }

    public DeployingDisplay(Identifier id, SandPaperPolishingRecipe recipe) {
        this(
            EntryIngredients.ofItemTag(AllItemTags.SANDPAPER),
            EntryIngredients.ofIngredient(recipe.ingredient()),
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
