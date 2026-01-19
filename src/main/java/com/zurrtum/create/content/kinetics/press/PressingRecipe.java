package com.zurrtum.create.content.kinetics.press;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public record PressingRecipe(ItemStackTemplate result, Ingredient ingredient) implements CreateSingleStackRecipe {
    @Override
    public RecipeSerializer<PressingRecipe> getSerializer() {
        return AllRecipeSerializers.PRESSING;
    }

    @Override
    public RecipeType<PressingRecipe> getType() {
        return AllRecipeTypes.PRESSING;
    }

    public static class Serializer implements RecipeSerializer<PressingRecipe> {
        public static final MapCodec<PressingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(PressingRecipe::result),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(PressingRecipe::ingredient)
        ).apply(instance, PressingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, PressingRecipe> PACKET_CODEC = StreamCodec.composite(
            ItemStackTemplate.STREAM_CODEC,
            PressingRecipe::result,
            Ingredient.CONTENTS_STREAM_CODEC,
            PressingRecipe::ingredient,
            PressingRecipe::new
        );

        @Override
        public MapCodec<PressingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, PressingRecipe> streamCodec() {
            return PACKET_CODEC;
        }
    }
}
