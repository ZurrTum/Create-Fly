package com.zurrtum.create.content.fluids.transfer;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRecipe;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public record EmptyingRecipe(ItemStackTemplate result, FluidStack fluidResult, Ingredient ingredient) implements CreateSingleStackRecipe {
    @Override
    public RecipeSerializer<EmptyingRecipe> getSerializer() {
        return AllRecipeSerializers.EMPTYING;
    }

    @Override
    public RecipeType<EmptyingRecipe> getType() {
        return AllRecipeTypes.EMPTYING;
    }

    public static class Serializer implements RecipeSerializer<EmptyingRecipe> {
        public static final MapCodec<EmptyingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(EmptyingRecipe::result),
            FluidStack.CODEC.fieldOf("fluid_result").forGetter(EmptyingRecipe::fluidResult),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(EmptyingRecipe::ingredient)
        ).apply(instance, EmptyingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, EmptyingRecipe> PACKET_CODEC = StreamCodec.composite(
            ItemStackTemplate.STREAM_CODEC,
            EmptyingRecipe::result,
            FluidStack.PACKET_CODEC,
            EmptyingRecipe::fluidResult,
            Ingredient.CONTENTS_STREAM_CODEC,
            EmptyingRecipe::ingredient,
            EmptyingRecipe::new
        );

        @Override
        public MapCodec<EmptyingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, EmptyingRecipe> streamCodec() {
            return PACKET_CODEC;
        }
    }
}
