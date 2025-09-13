package com.zurrtum.create.content.kinetics.crusher;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.processing.recipe.ChanceOutput;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;

import java.util.List;

public record CrushingRecipe(int time, List<ChanceOutput> results, Ingredient ingredient) implements AbstractCrushingRecipe {
    @Override
    public RecipeSerializer<CrushingRecipe> getSerializer() {
        return AllRecipeSerializers.CRUSHING;
    }

    @Override
    public RecipeType<CrushingRecipe> getType() {
        return AllRecipeTypes.CRUSHING;
    }

    public static class Serializer implements RecipeSerializer<CrushingRecipe> {
        public static final MapCodec<CrushingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("processing_time").forGetter(CrushingRecipe::time),
            ChanceOutput.CODEC.listOf(1, 5).fieldOf("results").forGetter(CrushingRecipe::results),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(CrushingRecipe::ingredient)
        ).apply(instance, CrushingRecipe::new));

        public static final PacketCodec<RegistryByteBuf, CrushingRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER,
            CrushingRecipe::time,
            ChanceOutput.PACKET_CODEC.collect(PacketCodecs.toList()),
            CrushingRecipe::results,
            Ingredient.PACKET_CODEC,
            CrushingRecipe::ingredient,
            CrushingRecipe::new
        );

        @Override
        public MapCodec<CrushingRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, CrushingRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
