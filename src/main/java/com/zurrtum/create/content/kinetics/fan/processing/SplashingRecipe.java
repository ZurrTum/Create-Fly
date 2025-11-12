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

public record SplashingRecipe(List<ChanceOutput> results, Ingredient ingredient) implements CreateSingleStackRollableRecipe {
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
            ChanceOutput.CODEC.listOf(1, 2).fieldOf("results").forGetter(SplashingRecipe::results),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(SplashingRecipe::ingredient)
        ).apply(instance, SplashingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, SplashingRecipe> PACKET_CODEC = StreamCodec.composite(
            ChanceOutput.PACKET_CODEC.apply(ByteBufCodecs.list()),
            SplashingRecipe::results,
            Ingredient.CONTENTS_STREAM_CODEC,
            SplashingRecipe::ingredient,
            SplashingRecipe::new
        );

        @Override
        public MapCodec<SplashingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SplashingRecipe> streamCodec() {
            return PACKET_CODEC;
        }
    }
}
