package com.zurrtum.create.content.kinetics.mixer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.processing.basin.BasinInput;
import com.zurrtum.create.content.processing.basin.BasinRecipe;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.foundation.recipe.TimedRecipe;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public record MixingRecipe(
    int time, List<ProcessingOutput> results, List<FluidStack> fluidResults, HeatCondition heat, List<FluidIngredient> fluidIngredients,
    List<SizedIngredient> ingredients
) implements BasinRecipe, TimedRecipe {
    @Override
    public int getIngredientSize() {
        return fluidIngredients.size() + ingredients().size();
    }

    @Override
    public boolean matches(BasinInput input, Level world) {
        if (!heat.testBlazeBurner(input.heat())) {
            return false;
        }
        ServerFilteringBehaviour filter = input.filter();
        if (filter == null) {
            return false;
        }
        if (results.isEmpty()) {
            if (!filter.test(fluidResults.getFirst())) {
                return false;
            }
        } else if (!filter.test(results.getFirst().create())) {
            return false;
        }
        List<ItemStack> outputs = BasinRecipe.tryCraft(input, ingredients);
        if (outputs == null) {
            return false;
        }
        if (!BasinRecipe.matchFluidIngredient(input, fluidIngredients)) {
            return false;
        }
        ProcessingOutput.rollOutput(input.random(), results, outputs::add);
        return input.acceptOutputs(outputs, fluidResults, true);
    }

    @Override
    public boolean apply(BasinInput input) {
        if (!heat.testBlazeBurner(input.heat())) {
            return false;
        }
        Deque<Runnable> changes = new ArrayDeque<>();
        List<ItemStack> outputs = BasinRecipe.prepareCraft(input, ingredients, changes);
        if (outputs == null) {
            return false;
        }
        if (!BasinRecipe.prepareFluidCraft(input, fluidIngredients, changes)) {
            return false;
        }
        ProcessingOutput.rollOutput(input.random(), results, outputs::add);
        if (!input.acceptOutputs(outputs, fluidResults, true)) {
            return false;
        }
        changes.forEach(Runnable::run);
        return input.acceptOutputs(outputs, fluidResults, false);
    }

    @Override
    public RecipeSerializer<MixingRecipe> getSerializer() {
        return AllRecipeSerializers.MIXING;
    }

    @Override
    public RecipeType<MixingRecipe> getType() {
        return AllRecipeTypes.MIXING;
    }

    public static class Serializer implements RecipeSerializer<MixingRecipe> {
        public static final MapCodec<MixingRecipe> CODEC = RecordCodecBuilder.mapCodec((Instance<MixingRecipe> instance) -> instance.group(
            Codec.INT.optionalFieldOf("processing_time", 100).forGetter(MixingRecipe::time),
            ProcessingOutput.CODEC.listOf(1, 4).optionalFieldOf("results", List.of()).forGetter(MixingRecipe::results),
            FluidStack.CODEC.listOf(1, 2).optionalFieldOf("fluid_results", List.of()).forGetter(MixingRecipe::fluidResults),
            HeatCondition.CODEC.optionalFieldOf("heat_requirement", HeatCondition.NONE).forGetter(MixingRecipe::heat),
            FluidIngredient.CODEC.listOf(1, 2).optionalFieldOf("fluid_ingredients", List.of()).forGetter(MixingRecipe::fluidIngredients),
            SizedIngredient.LIST_CODEC.optionalFieldOf("ingredients", List.of()).forGetter(MixingRecipe::ingredients)
        ).apply(instance, MixingRecipe::new)).validate(recipe -> {
            if (recipe.results.isEmpty() && recipe.fluidResults.isEmpty()) {
                return DataResult.error(() -> "MixingRecipe must have a result or a fluid result");
            }
            if (recipe.fluidIngredients.isEmpty() && recipe.ingredients.isEmpty()) {
                return DataResult.error(() -> "MixingRecipe must have a ingredient or a fluid ingredient");
            }
            if (recipe.ingredients.size() > 9) {
                return DataResult.error(() -> "Ingredients type is too many: " + recipe.ingredients.size() + ", expected range [0-9]");
            }
            return DataResult.success(recipe);
        });
        private static final StreamCodec<RegistryFriendlyByteBuf, MixingRecipe> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            MixingRecipe::time,
            ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()),
            MixingRecipe::results,
            FluidStack.PACKET_CODEC.apply(ByteBufCodecs.list()),
            MixingRecipe::fluidResults,
            HeatCondition.PACKET_CODEC,
            MixingRecipe::heat,
            FluidIngredient.PACKET_CODEC.apply(ByteBufCodecs.list()),
            MixingRecipe::fluidIngredients,
            SizedIngredient.PACKET_CODEC.apply(ByteBufCodecs.list()),
            MixingRecipe::ingredients,
            MixingRecipe::new
        );

        @Override
        public MapCodec<MixingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MixingRecipe> streamCodec() {
            return PACKET_CODEC;
        }
    }
}
