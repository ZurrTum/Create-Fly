package com.zurrtum.create.content.fluids.transfer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.foundation.recipe.CreateRecipe;
import com.zurrtum.create.infrastructure.component.SequencedAssemblyJunk;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record FillingRecipe(ItemStackTemplate result, Ingredient ingredient, FluidIngredient fluidIngredient) implements CreateRecipe<FillingInput> {
    @Override
    public boolean matches(FillingInput input, Level world) {
        return ingredient.test(input.item()) && fluidIngredient.test(input.fluid());
    }

    @Override
    public ItemStack assemble(FillingInput input) {
        SequencedAssemblyJunk junk = input.item().get(AllDataComponents.SEQUENCED_ASSEMBLY_JUNK);
        if (junk != null && junk.hasJunk()) {
            return junk.getJunk();
        }
        return result.create();
    }

    @Override
    public RecipeSerializer<FillingRecipe> getSerializer() {
        return AllRecipeSerializers.FILLING;
    }

    @Override
    public RecipeType<FillingRecipe> getType() {
        return AllRecipeTypes.FILLING;
    }

    public static Component getDescriptionForAssembly(DynamicOps<JsonElement> ops, JsonObject object) {
        return FluidIngredient.CODEC.parse(JsonOps.INSTANCE, object.get("fluid_ingredient")).result()
            .flatMap(fluidIngredient -> fluidIngredient.getMatchingFluidStacks().stream().findFirst())
            .map(stack -> Component.translatable("create.recipe.assembly.spout_filling_fluid", stack.getName().getString()))
            .orElseGet(() -> Component.literal("Invalid"));
    }

    public static class Serializer implements RecipeSerializer<FillingRecipe> {
        public static final MapCodec<FillingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(FillingRecipe::result),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(FillingRecipe::ingredient),
            FluidIngredient.CODEC.fieldOf("fluid_ingredient").forGetter(FillingRecipe::fluidIngredient)
        ).apply(instance, FillingRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, FillingRecipe> PACKET_CODEC = StreamCodec.composite(
            ItemStackTemplate.STREAM_CODEC,
            FillingRecipe::result,
            Ingredient.CONTENTS_STREAM_CODEC,
            FillingRecipe::ingredient,
            FluidIngredient.PACKET_CODEC,
            FillingRecipe::fluidIngredient,
            FillingRecipe::new
        );

        @Override
        public MapCodec<FillingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FillingRecipe> streamCodec() {
            return PACKET_CODEC;
        }
    }
}
