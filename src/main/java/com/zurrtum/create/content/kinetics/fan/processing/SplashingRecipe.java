package com.zurrtum.create.content.kinetics.fan.processing;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRollableRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

public record SplashingRecipe(List<ProcessingOutput> results, Ingredient ingredient) implements CreateSingleStackRollableRecipe {
    public static final MapCodec<SplashingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ProcessingOutput.CODEC.listOf(1, 12).fieldOf("results").forGetter(SplashingRecipe::results),
        Ingredient.CODEC.fieldOf("ingredient").forGetter(SplashingRecipe::ingredient)
    ).apply(instance, SplashingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SplashingRecipe> STREAM_CODEC = StreamCodec.composite(
        ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()),
        SplashingRecipe::results,
        Ingredient.CONTENTS_STREAM_CODEC,
        SplashingRecipe::ingredient,
        SplashingRecipe::new
    );
    public static final RecipeSerializer<SplashingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public RecipeSerializer<SplashingRecipe> getSerializer() {
        return AllRecipeSerializers.SPLASHING;
    }

    @Override
    public RecipeType<SplashingRecipe> getType() {
        return AllRecipeTypes.SPLASHING;
    }
}
