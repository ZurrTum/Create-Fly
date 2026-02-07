package com.zurrtum.create.content.kinetics.fan.processing;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRollableRecipe;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;

import java.util.List;

public record SplashingRecipe(List<ProcessingOutput> results,
                              Ingredient ingredient) implements CreateSingleStackRollableRecipe {
    @Override
    public RecipeSerializer<SplashingRecipe> getSerializer() {
        return AllRecipeSerializers.SPLASHING;
    }

    @Override
    public RecipeType<SplashingRecipe> getType() {
        return AllRecipeTypes.SPLASHING;
    }

    public static class Serializer implements RecipeSerializer<SplashingRecipe> {
        public static final MapCodec<SplashingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ProcessingOutput.CODEC.listOf(1, 12).fieldOf("results").forGetter(SplashingRecipe::results),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(SplashingRecipe::ingredient)
        ).apply(instance, SplashingRecipe::new));

        public static final PacketCodec<RegistryByteBuf, SplashingRecipe> PACKET_CODEC = PacketCodec.tuple(
            ProcessingOutput.STREAM_CODEC.collect(PacketCodecs.toList()),
            SplashingRecipe::results,
            Ingredient.PACKET_CODEC,
            SplashingRecipe::ingredient,
            SplashingRecipe::new
        );

        @Override
        public MapCodec<SplashingRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, SplashingRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
