package com.zurrtum.create.content.kinetics.millstone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.kinetics.crusher.AbstractCrushingRecipe;
import com.zurrtum.create.content.processing.recipe.ChanceOutput;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;

import java.util.List;

public record MillingRecipe(int time, List<ChanceOutput> results, Ingredient ingredient) implements AbstractCrushingRecipe {
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
            ChanceOutput.CODEC.listOf(1, 3).fieldOf("results").forGetter(MillingRecipe::results),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(MillingRecipe::ingredient)
        ).apply(instance, MillingRecipe::new));

        public static final PacketCodec<RegistryByteBuf, MillingRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER,
            MillingRecipe::time,
            ChanceOutput.PACKET_CODEC.collect(PacketCodecs.toList()),
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
