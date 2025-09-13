package com.zurrtum.create.content.kinetics.saw;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;

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

        public static final PacketCodec<RegistryByteBuf, CuttingRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER,
            CuttingRecipe::time,
            ItemStack.PACKET_CODEC,
            CuttingRecipe::result,
            Ingredient.PACKET_CODEC,
            CuttingRecipe::ingredient,
            CuttingRecipe::new
        );

        @Override
        public MapCodec<CuttingRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, CuttingRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
