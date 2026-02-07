package com.zurrtum.create.content.kinetics.press;

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

public record PressingRecipe(List<ProcessingOutput> results,
                             Ingredient ingredient) implements CreateSingleStackRollableRecipe {
    @Override
    public RecipeSerializer<PressingRecipe> getSerializer() {
        return AllRecipeSerializers.PRESSING;
    }

    @Override
    public RecipeType<PressingRecipe> getType() {
        return AllRecipeTypes.PRESSING;
    }

    public static class Serializer implements RecipeSerializer<PressingRecipe> {
        public static final MapCodec<PressingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ProcessingOutput.CODEC.listOf(1, 2).fieldOf("results").forGetter(PressingRecipe::results),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(PressingRecipe::ingredient)
        ).apply(instance, PressingRecipe::new));

        public static final PacketCodec<RegistryByteBuf, PressingRecipe> PACKET_CODEC = PacketCodec.tuple(
            ProcessingOutput.STREAM_CODEC.collect(PacketCodecs.toList()),
            PressingRecipe::results,
            Ingredient.PACKET_CODEC,
            PressingRecipe::ingredient,
            PressingRecipe::new
        );

        @Override
        public MapCodec<PressingRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, PressingRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
