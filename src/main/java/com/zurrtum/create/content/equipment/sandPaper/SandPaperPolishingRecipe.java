package com.zurrtum.create.content.equipment.sandPaper;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;

public record SandPaperPolishingRecipe(ItemStackTemplate result, Ingredient ingredient) implements CreateSingleStackRecipe {
    public static final MapCodec<SandPaperPolishingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ItemStackTemplate.CODEC.fieldOf("result").forGetter(SandPaperPolishingRecipe::result),
        Ingredient.CODEC.fieldOf("ingredient").forGetter(SandPaperPolishingRecipe::ingredient)
    ).apply(instance, SandPaperPolishingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SandPaperPolishingRecipe> STREAM_CODEC = StreamCodec.composite(
        ItemStackTemplate.STREAM_CODEC,
        SandPaperPolishingRecipe::result,
        Ingredient.CONTENTS_STREAM_CODEC,
        SandPaperPolishingRecipe::ingredient,
        SandPaperPolishingRecipe::new
    );
    public static final RecipeSerializer<SandPaperPolishingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return AllRecipeSerializers.SANDPAPER_POLISHING;
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return AllRecipeTypes.SANDPAPER_POLISHING;
    }
}
