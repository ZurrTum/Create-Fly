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

public record HauntingRecipe(List<ProcessingOutput> results,
                             Ingredient ingredient) implements CreateSingleStackRollableRecipe {
    @Override
    public RecipeSerializer<HauntingRecipe> getSerializer() {
        return AllRecipeSerializers.HAUNTING;
    }

    @Override
    public RecipeType<HauntingRecipe> getType() {
        return AllRecipeTypes.HAUNTING;
    }

    public static class Serializer implements RecipeSerializer<HauntingRecipe> {
        public static final MapCodec<HauntingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ProcessingOutput.CODEC.listOf(1, 12).fieldOf("results").forGetter(HauntingRecipe::results),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(HauntingRecipe::ingredient)
        ).apply(instance, HauntingRecipe::new));

        public static final PacketCodec<RegistryByteBuf, HauntingRecipe> PACKET_CODEC = PacketCodec.tuple(
            ProcessingOutput.STREAM_CODEC.collect(PacketCodecs.toList()),
            HauntingRecipe::results,
            Ingredient.PACKET_CODEC,
            HauntingRecipe::ingredient,
            HauntingRecipe::new
        );

        @Override
        public MapCodec<HauntingRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, HauntingRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
