package com.zurrtum.create.content.kinetics.crusher;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.processing.recipe.ChanceOutput;

import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

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

        public static final StreamCodec<RegistryFriendlyByteBuf, CrushingRecipe> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            CrushingRecipe::time,
            ChanceOutput.PACKET_CODEC.apply(ByteBufCodecs.list()),
            CrushingRecipe::results,
            Ingredient.CONTENTS_STREAM_CODEC,
            CrushingRecipe::ingredient,
            CrushingRecipe::new
        );

        @Override
        public MapCodec<CrushingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CrushingRecipe> streamCodec() {
            return PACKET_CODEC;
        }
    }
}
