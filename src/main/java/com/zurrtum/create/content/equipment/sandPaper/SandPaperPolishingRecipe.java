package com.zurrtum.create.content.equipment.sandPaper;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.SingleStackRecipeInput;

public record SandPaperPolishingRecipe(ItemStack result, Ingredient ingredient) implements CreateSingleStackRecipe {
    @Override
    public RecipeSerializer<? extends Recipe<SingleStackRecipeInput>> getSerializer() {
        return AllRecipeSerializers.SANDPAPER_POLISHING;
    }

    @Override
    public RecipeType<? extends Recipe<SingleStackRecipeInput>> getType() {
        return AllRecipeTypes.SANDPAPER_POLISHING;
    }

    public static class Serializer implements RecipeSerializer<SandPaperPolishingRecipe> {
        public static final MapCodec<SandPaperPolishingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStack.CODEC.fieldOf("result").forGetter(SandPaperPolishingRecipe::result),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(SandPaperPolishingRecipe::ingredient)
        ).apply(instance, SandPaperPolishingRecipe::new));

        public static final PacketCodec<RegistryByteBuf, SandPaperPolishingRecipe> PACKET_CODEC = PacketCodec.tuple(
            ItemStack.PACKET_CODEC,
            SandPaperPolishingRecipe::result,
            Ingredient.PACKET_CODEC,
            SandPaperPolishingRecipe::ingredient,
            SandPaperPolishingRecipe::new
        );

        @Override
        public MapCodec<SandPaperPolishingRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, SandPaperPolishingRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
