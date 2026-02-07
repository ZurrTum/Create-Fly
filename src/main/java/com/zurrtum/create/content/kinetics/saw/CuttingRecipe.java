package com.zurrtum.create.content.kinetics.saw;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRollableRecipe;
import com.zurrtum.create.foundation.recipe.TimedRecipe;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;

import java.util.List;

public record CuttingRecipe(int time, List<ProcessingOutput> results,
                            Ingredient ingredient) implements CreateSingleStackRollableRecipe, TimedRecipe {
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
            ProcessingOutput.CODEC.listOf(1, 4).fieldOf("results").forGetter(CuttingRecipe::results),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(CuttingRecipe::ingredient)
        ).apply(instance, CuttingRecipe::new));

        public static final PacketCodec<RegistryByteBuf, CuttingRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER,
            CuttingRecipe::time,
            ProcessingOutput.STREAM_CODEC.collect(PacketCodecs.toList()),
            CuttingRecipe::results,
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
