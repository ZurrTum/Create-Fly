package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.Create;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public record FanBlastingDisplay(EntryIngredient input, EntryIngredient output, Optional<ResourceLocation> location) implements Display {
    public static final DisplaySerializer<FanBlastingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().fieldOf("input").forGetter(FanBlastingDisplay::input),
            EntryIngredient.codec().fieldOf("output").forGetter(FanBlastingDisplay::output),
            ResourceLocation.CODEC.optionalFieldOf("location").forGetter(FanBlastingDisplay::location)
        ).apply(instance, FanBlastingDisplay::new)), StreamCodec.composite(
            EntryIngredient.streamCodec(),
            FanBlastingDisplay::input,
            EntryIngredient.streamCodec(),
            FanBlastingDisplay::output,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
            FanBlastingDisplay::location,
            FanBlastingDisplay::new
        )
    );

    public static Display of(RecipeHolder<?> entry) {
        if (!AllRecipeTypes.CAN_BE_AUTOMATED.test(entry)) {
            return null;
        }
        SingleItemRecipe recipe = (SingleItemRecipe) entry.value();
        Ingredient ingredient = recipe.input();
        Optional<ItemStack> firstInput = ingredient.values.stream().findFirst().map(item -> item.value().getDefaultInstance());
        if (firstInput.isEmpty()) {
            return null;
        }
        SingleRecipeInput input = new SingleRecipeInput(firstInput.get());
        MinecraftServer server = Create.SERVER;
        ServerLevel world = server.getLevel(Level.OVERWORLD);
        RecipeManager recipeManager = server.getRecipeManager();
        if (recipe instanceof SmeltingRecipe) {
            Optional<RecipeHolder<BlastingRecipe>> blastingRecipe = recipeManager.getRecipeFor(RecipeType.BLASTING, input, world)
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED);
            if (blastingRecipe.isPresent()) {
                return null;
            }
        }
        Optional<RecipeHolder<SmokingRecipe>> smokingRecipe = recipeManager.getRecipeFor(RecipeType.SMOKING, input, world)
            .filter(AllRecipeTypes.CAN_BE_AUTOMATED);
        if (smokingRecipe.isPresent()) {
            return null;
        }
        return new FanBlastingDisplay(
            EntryIngredients.ofIngredient(ingredient),
            EntryIngredients.of(recipe.result()),
            Optional.of(entry.id().location())
        );
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
        return ReiCommonPlugin.FAN_BLASTING;
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
