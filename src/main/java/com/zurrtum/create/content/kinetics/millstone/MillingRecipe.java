package com.zurrtum.create.content.kinetics.millstone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRollableRecipe;
import com.zurrtum.create.foundation.recipe.TimedRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

public record MillingRecipe(int time, List<ProcessingOutput> results, Ingredient ingredient) implements CreateSingleStackRollableRecipe, TimedRecipe {
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

        public static final StreamCodec<RegistryFriendlyByteBuf, MillingRecipe> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            MillingRecipe::time,
            ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()),
            MillingRecipe::results,
            Ingredient.CONTENTS_STREAM_CODEC,
            MillingRecipe::ingredient,
            MillingRecipe::new
        );

        @Override
        public MapCodec<MillingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MillingRecipe> streamCodec() {
            return PACKET_CODEC;
        }
    }
}
