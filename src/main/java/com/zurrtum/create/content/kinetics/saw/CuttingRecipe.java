package com.zurrtum.create.content.kinetics.saw;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public record CuttingRecipe(int time, ItemStack result, Ingredient ingredient) implements CreateSingleStackRecipe {
    @Override
    public RecipeSerializer<CuttingRecipe> getSerializer() {
        return AllRecipeSerializers.CUTTING;
    }

    @Override
    public RecipeType<CuttingRecipe> getType() {
        return AllRecipeTypes.CUTTING;
    }

    public static class Serializer implements RecipeSerializer<CuttingRecipe> {
        public static final MapCodec<CuttingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("processing_time").forGetter(CuttingRecipe::time),
            ItemStack.CODEC.fieldOf("result").forGetter(CuttingRecipe::result),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(CuttingRecipe::ingredient)
        ).apply(instance, CuttingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CuttingRecipe> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            CuttingRecipe::time,
            ItemStack.STREAM_CODEC,
            CuttingRecipe::result,
            Ingredient.CONTENTS_STREAM_CODEC,
            CuttingRecipe::ingredient,
            CuttingRecipe::new
        );

        @Override
        public MapCodec<CuttingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CuttingRecipe> streamCodec() {
            return PACKET_CODEC;
        }
    }
}
