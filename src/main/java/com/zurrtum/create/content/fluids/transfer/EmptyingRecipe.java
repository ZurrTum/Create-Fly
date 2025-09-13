package com.zurrtum.create.content.fluids.transfer;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRecipe;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;

public record EmptyingRecipe(ItemStack result, FluidStack fluidResult, Ingredient ingredient) implements CreateSingleStackRecipe {
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
            ItemStack.CODEC.fieldOf("result").forGetter(EmptyingRecipe::result),
            FluidStack.CODEC.fieldOf("fluid_result").forGetter(EmptyingRecipe::fluidResult),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(EmptyingRecipe::ingredient)
        ).apply(instance, EmptyingRecipe::new));

        public static final PacketCodec<RegistryByteBuf, EmptyingRecipe> PACKET_CODEC = PacketCodec.tuple(
            ItemStack.PACKET_CODEC,
            EmptyingRecipe::result,
            FluidStack.PACKET_CODEC,
            EmptyingRecipe::fluidResult,
            Ingredient.PACKET_CODEC,
            EmptyingRecipe::ingredient,
            EmptyingRecipe::new
        );

        @Override
        public MapCodec<EmptyingRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, EmptyingRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
