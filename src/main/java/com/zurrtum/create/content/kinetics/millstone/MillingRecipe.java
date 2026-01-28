package com.zurrtum.create.content.kinetics.millstone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.kinetics.crusher.AbstractCrushingRecipe;
import com.zurrtum.create.content.processing.recipe.ChanceOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

public record MillingRecipe(int time, List<ChanceOutput> results, Ingredient ingredient) implements AbstractCrushingRecipe {
    public static final MapCodec<MillingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.INT.fieldOf("processing_time").forGetter(MillingRecipe::time),
        ChanceOutput.CODEC.listOf(1, 3).fieldOf("results").forGetter(MillingRecipe::results),
        Ingredient.CODEC.fieldOf("ingredient").forGetter(MillingRecipe::ingredient)
    ).apply(instance, MillingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MillingRecipe> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        MillingRecipe::time,
        ChanceOutput.PACKET_CODEC.apply(ByteBufCodecs.list()),
        MillingRecipe::results,
        Ingredient.CONTENTS_STREAM_CODEC,
        MillingRecipe::ingredient,
        MillingRecipe::new
    );
    public static final RecipeSerializer<MillingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public RecipeSerializer<MillingRecipe> getSerializer() {
        return AllRecipeSerializers.MILLING;
    }

    @Override
    public RecipeType<MillingRecipe> getType() {
        return AllRecipeTypes.MILLING;
    }
}
