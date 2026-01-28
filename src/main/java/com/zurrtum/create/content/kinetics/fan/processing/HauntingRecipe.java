package com.zurrtum.create.content.kinetics.fan.processing;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.processing.recipe.ChanceOutput;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRollableRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

public record HauntingRecipe(List<ChanceOutput> results, Ingredient ingredient) implements CreateSingleStackRollableRecipe {
    public static final MapCodec<HauntingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ChanceOutput.CODEC.listOf(1, 2).fieldOf("results").forGetter(HauntingRecipe::results),
        Ingredient.CODEC.fieldOf("ingredient").forGetter(HauntingRecipe::ingredient)
    ).apply(instance, HauntingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, HauntingRecipe> STREAM_CODEC = StreamCodec.composite(
        ChanceOutput.PACKET_CODEC.apply(ByteBufCodecs.list()),
        HauntingRecipe::results,
        Ingredient.CONTENTS_STREAM_CODEC,
        HauntingRecipe::ingredient,
        HauntingRecipe::new
    );
    public static final RecipeSerializer<HauntingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public RecipeSerializer<HauntingRecipe> getSerializer() {
        return AllRecipeSerializers.HAUNTING;
    }

    @Override
    public RecipeType<HauntingRecipe> getType() {
        return AllRecipeTypes.HAUNTING;
    }
}
