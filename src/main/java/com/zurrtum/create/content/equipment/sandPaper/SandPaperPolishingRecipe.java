package com.zurrtum.create.content.equipment.sandPaper;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;

public record SandPaperPolishingRecipe(ItemStack result, Ingredient ingredient) implements CreateSingleStackRecipe {
    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return AllRecipeSerializers.SANDPAPER_POLISHING;
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return AllRecipeTypes.SANDPAPER_POLISHING;
    }

    public static class Serializer implements RecipeSerializer<SandPaperPolishingRecipe> {
        public static final MapCodec<SandPaperPolishingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStack.CODEC.fieldOf("result").forGetter(SandPaperPolishingRecipe::result),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(SandPaperPolishingRecipe::ingredient)
        ).apply(instance, SandPaperPolishingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, SandPaperPolishingRecipe> PACKET_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC,
            SandPaperPolishingRecipe::result,
            Ingredient.CONTENTS_STREAM_CODEC,
            SandPaperPolishingRecipe::ingredient,
            SandPaperPolishingRecipe::new
        );

        @Override
        public MapCodec<SandPaperPolishingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SandPaperPolishingRecipe> streamCodec() {
            return PACKET_CODEC;
        }
    }
}
