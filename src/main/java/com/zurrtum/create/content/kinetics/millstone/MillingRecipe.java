package com.zurrtum.create.content.kinetics.millstone;

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

public record MillingRecipe(int time, List<ProcessingOutput> results,
                            Ingredient ingredient) implements CreateSingleStackRollableRecipe, TimedRecipe {
    @Override
    public RecipeSerializer<MillingRecipe> getSerializer() {
        return AllRecipeSerializers.MILLING;
    }

    @Override
    public RecipeType<MillingRecipe> getType() {
        return AllRecipeTypes.MILLING;
    }

    public static class Serializer implements RecipeSerializer<MillingRecipe> {
        public static final MapCodec<MillingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("processing_time").forGetter(MillingRecipe::time),
            ProcessingOutput.CODEC.listOf(1, 4).fieldOf("results").forGetter(MillingRecipe::results),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(MillingRecipe::ingredient)
        ).apply(instance, MillingRecipe::new));

        public static final PacketCodec<RegistryByteBuf, MillingRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER,
            MillingRecipe::time,
            ProcessingOutput.STREAM_CODEC.collect(PacketCodecs.toList()),
            MillingRecipe::results,
            Ingredient.PACKET_CODEC,
            MillingRecipe::ingredient,
            MillingRecipe::new
        );

        @Override
        public MapCodec<MillingRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, MillingRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
