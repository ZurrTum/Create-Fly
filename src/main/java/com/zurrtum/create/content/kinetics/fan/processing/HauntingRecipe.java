package com.zurrtum.create.content.kinetics.fan.processing;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.processing.recipe.ChanceOutput;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRollableRecipe;

import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public record HauntingRecipe(List<ChanceOutput> results, Ingredient ingredient) implements CreateSingleStackRollableRecipe {
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
            ChanceOutput.CODEC.listOf(1, 2).fieldOf("results").forGetter(HauntingRecipe::results),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(HauntingRecipe::ingredient)
        ).apply(instance, HauntingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, HauntingRecipe> PACKET_CODEC = StreamCodec.composite(
            ChanceOutput.PACKET_CODEC.apply(ByteBufCodecs.list()),
            HauntingRecipe::results,
            Ingredient.CONTENTS_STREAM_CODEC,
            HauntingRecipe::ingredient,
            HauntingRecipe::new
        );

        @Override
        public MapCodec<HauntingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, HauntingRecipe> streamCodec() {
            return PACKET_CODEC;
        }
    }
}
