package com.zurrtum.create.content.kinetics.mixer;

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

public record CompactingRecipe(
    List<ProcessingOutput> results, HeatCondition heat, List<FluidIngredient> fluidIngredients, List<SizedIngredient> ingredients
) implements BasinRecipe {
    public static final MapCodec<CompactingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec((Instance<CompactingRecipe> instance) -> instance.group(
        ProcessingOutput.CODEC.listOf(1, 4).fieldOf("results").forGetter(CompactingRecipe::results),
        HeatCondition.CODEC.optionalFieldOf("heat_requirement", HeatCondition.NONE).forGetter(CompactingRecipe::heat),
        FluidIngredient.CODEC.listOf(1, 2).optionalFieldOf("fluid_ingredients", List.of()).forGetter(CompactingRecipe::fluidIngredients),
        SizedIngredient.LIST_CODEC.optionalFieldOf("ingredients", List.of()).forGetter(CompactingRecipe::ingredients)
    ).apply(instance, CompactingRecipe::new)).validate(recipe -> {
        if (recipe.fluidIngredients.isEmpty() && recipe.ingredients.isEmpty()) {
            return DataResult.error(() -> "CompactingRecipe must have a ingredient or a fluid ingredient");
        }
        if (recipe.ingredients.size() > 9) {
            return DataResult.error(() -> "Ingredients type is too many: " + recipe.ingredients.size() + ", expected range [0-9]");
        }
        return DataResult.success(recipe);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, CompactingRecipe> STREAM_CODEC = StreamCodec.composite(
        ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()),
        CompactingRecipe::results,
        HeatCondition.PACKET_CODEC,
        CompactingRecipe::heat,
        FluidIngredient.PACKET_CODEC.apply(ByteBufCodecs.list()),
        CompactingRecipe::fluidIngredients,
        SizedIngredient.PACKET_CODEC.apply(ByteBufCodecs.list()),
        CompactingRecipe::ingredients,
        CompactingRecipe::new
    );
    public static final RecipeSerializer<CompactingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public int getIngredientSize() {
        return fluidIngredients.size() + ingredients.size();
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
        if (!filter.test(results.getFirst().create())) {
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
        return input.acceptOutputs(outputs, List.of(), true);
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
        if (!input.acceptOutputs(outputs, List.of(), true)) {
            return false;
        }
        changes.forEach(Runnable::run);
        return input.acceptOutputs(outputs, List.of(), false);
    }

    @Override
    public RecipeSerializer<CompactingRecipe> getSerializer() {
        return AllRecipeSerializers.COMPACTING;
    }

    @Override
    public RecipeType<CompactingRecipe> getType() {
        return AllRecipeTypes.COMPACTING;
    }
}
