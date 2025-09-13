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
import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public record MixingRecipe(
    ItemStack result, FluidStack fluidResult, HeatCondition heat, List<FluidIngredient> fluidIngredients, List<SizedIngredient> ingredients
) implements BasinRecipe {
    @Override
    public int getIngredientSize() {
        return fluidIngredients.size() + ingredients().size();
    }

    @Override
    public boolean matches(BasinInput input, World world) {
        if (!heat.testBlazeBurner(input.getHeatLevel())) {
            return false;
        }
        List<ItemStack> outputs = BasinRecipe.tryCraft(input, ingredients);
        if (outputs == null) {
            return false;
        }
        if (!BasinRecipe.matchFluidIngredient(input, fluidIngredients)) {
            return false;
        }
        if (!result.isEmpty()) {
            outputs.add(result);
        }
        List<FluidStack> fluids = fluidResult.isEmpty() ? List.of() : List.of(fluidResult);
        return input.acceptOutputs(outputs, fluids, true);
    }

    @Override
    public boolean apply(BasinInput input) {
        if (!heat.testBlazeBurner(input.getHeatLevel())) {
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
        if (!result.isEmpty()) {
            outputs.add(result);
        }
        List<FluidStack> fluids = fluidResult.isEmpty() ? List.of() : List.of(fluidResult);
        if (!input.acceptOutputs(outputs, fluids, true)) {
            return false;
        }
        changes.forEach(Runnable::run);
        return input.acceptOutputs(outputs, fluids, false);
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
            ItemStack.CODEC.optionalFieldOf("result", ItemStack.EMPTY).forGetter(MixingRecipe::result),
            FluidStack.CODEC.optionalFieldOf("fluid_result", FluidStack.EMPTY).forGetter(MixingRecipe::fluidResult),
            HeatCondition.CODEC.optionalFieldOf("heat_requirement", HeatCondition.NONE).forGetter(MixingRecipe::heat),
            FluidIngredient.CODEC.listOf(0, 2).optionalFieldOf("fluid_ingredients", List.of()).forGetter(MixingRecipe::fluidIngredients),
            SizedIngredient.getListCodec(0, 4).optionalFieldOf("ingredients", List.of()).forGetter(MixingRecipe::ingredients)
        ).apply(instance, MixingRecipe::new)).validate(recipe -> {
            if (recipe.result.isEmpty() && recipe.fluidResult.isEmpty()) {
                return DataResult.error(() -> "MixingRecipe must have a result or a fluid result");
            } else if (recipe.fluidIngredients.isEmpty() && recipe.ingredients.isEmpty()) {
                return DataResult.error(() -> "MixingRecipe must have a result or a fluid ingredient");
            }
            return DataResult.success(recipe);
        });
        private static final PacketCodec<RegistryByteBuf, MixingRecipe> PACKET_CODEC = PacketCodec.tuple(
            ItemStack.OPTIONAL_PACKET_CODEC,
            MixingRecipe::result,
            FluidStack.OPTIONAL_PACKET_CODEC,
            MixingRecipe::fluidResult,
            HeatCondition.PACKET_CODEC,
            MixingRecipe::heat,
            FluidIngredient.PACKET_CODEC.collect(PacketCodecs.toList()),
            MixingRecipe::fluidIngredients,
            SizedIngredient.PACKET_CODEC.collect(PacketCodecs.toList()),
            MixingRecipe::ingredients,
            MixingRecipe::new
        );

        @Override
        public MapCodec<MixingRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, MixingRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
