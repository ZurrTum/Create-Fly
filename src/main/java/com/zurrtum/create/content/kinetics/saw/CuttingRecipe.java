package com.zurrtum.create.content.kinetics.saw;

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

public record CuttingRecipe(int time, List<ProcessingOutput> results, Ingredient ingredient) implements CreateSingleStackRollableRecipe, TimedRecipe {
    public static final MapCodec<CuttingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.INT.fieldOf("processing_time").forGetter(CuttingRecipe::time),
        ProcessingOutput.CODEC.listOf(1, 4).fieldOf("results").forGetter(CuttingRecipe::results),
        Ingredient.CODEC.fieldOf("ingredient").forGetter(CuttingRecipe::ingredient)
    ).apply(instance, CuttingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, CuttingRecipe> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        CuttingRecipe::time,
        ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()),
        CuttingRecipe::results,
        Ingredient.CONTENTS_STREAM_CODEC,
        CuttingRecipe::ingredient,
        CuttingRecipe::new
    );
    public static final RecipeSerializer<CuttingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public RecipeSerializer<CuttingRecipe> getSerializer() {
        return AllRecipeSerializers.CUTTING;
    }

    @Override
    public RecipeType<CuttingRecipe> getType() {
        return AllRecipeTypes.CUTTING;
    }
}
