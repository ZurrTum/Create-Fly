package com.zurrtum.create.content.kinetics.crusher;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.processing.recipe.ChanceOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

public record CrushingRecipe(int time, List<ChanceOutput> results, Ingredient ingredient) implements AbstractCrushingRecipe {
    public static final MapCodec<CrushingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.INT.fieldOf("processing_time").forGetter(CrushingRecipe::time),
        ChanceOutput.CODEC.listOf(1, 5).fieldOf("results").forGetter(CrushingRecipe::results),
        Ingredient.CODEC.fieldOf("ingredient").forGetter(CrushingRecipe::ingredient)
    ).apply(instance, CrushingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, CrushingRecipe> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        CrushingRecipe::time,
        ChanceOutput.PACKET_CODEC.apply(ByteBufCodecs.list()),
        CrushingRecipe::results,
        Ingredient.CONTENTS_STREAM_CODEC,
        CrushingRecipe::ingredient,
        CrushingRecipe::new
    );
    public static final RecipeSerializer<CrushingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public RecipeSerializer<CrushingRecipe> getSerializer() {
        return AllRecipeSerializers.CRUSHING;
    }

    @Override
    public RecipeType<CrushingRecipe> getType() {
        return AllRecipeTypes.CRUSHING;
    }
}
