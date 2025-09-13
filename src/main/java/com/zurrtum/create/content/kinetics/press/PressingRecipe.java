package com.zurrtum.create.content.kinetics.press;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;

public record PressingRecipe(ItemStack result, Ingredient ingredient) implements CreateSingleStackRecipe {
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
            ItemStack.CODEC.fieldOf("result")
                .forGetter(PressingRecipe::result),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(PressingRecipe::ingredient)
        ).apply(instance, PressingRecipe::new));

        public static final PacketCodec<RegistryByteBuf, PressingRecipe> PACKET_CODEC = PacketCodec.tuple(
            ItemStack.PACKET_CODEC,
            PressingRecipe::result,
            Ingredient.PACKET_CODEC,
            PressingRecipe::ingredient,
            PressingRecipe::new
        );

        @Override
        public MapCodec<PressingRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, PressingRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
