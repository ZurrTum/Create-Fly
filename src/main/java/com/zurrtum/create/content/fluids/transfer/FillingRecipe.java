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
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public record FillingRecipe(ItemStack result, Ingredient ingredient, FluidIngredient fluidIngredient) implements CreateRecipe<FillingInput> {
    @Override
    public boolean matches(FillingInput input, World world) {
        return ingredient.test(input.item()) && fluidIngredient.test(input.fluid());
    }

    @Override
    public ItemStack craft(FillingInput input, RegistryWrapper.WrapperLookup registries) {
        SequencedAssemblyJunk junk = input.item().get(AllDataComponents.SEQUENCED_ASSEMBLY_JUNK);
        if (junk != null && junk.hasJunk()) {
            return junk.getJunk();
        }
        return result.copy();
    }

    @Override
    public RecipeSerializer<FillingRecipe> getSerializer() {
        return AllRecipeSerializers.FILLING;
    }

    @Override
    public RecipeType<FillingRecipe> getType() {
        return AllRecipeTypes.FILLING;
    }

    public static Text getDescriptionForAssembly(DynamicOps<JsonElement> ops, JsonObject object) {
        return FluidIngredient.CODEC.parse(JsonOps.INSTANCE, object.get("fluid_ingredient")).result()
            .flatMap(fluidIngredient -> fluidIngredient.getMatchingFluidStacks().stream().findFirst())
            .map(stack -> Text.translatable("create.recipe.assembly.spout_filling_fluid", stack.getName().getString()))
            .orElseGet(() -> Text.literal("Invalid"));
    }

    public static class Serializer implements RecipeSerializer<FillingRecipe> {
        public static final MapCodec<FillingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStack.CODEC.fieldOf("result").forGetter(FillingRecipe::result),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(FillingRecipe::ingredient),
            FluidIngredient.CODEC.fieldOf("fluid_ingredient").forGetter(FillingRecipe::fluidIngredient)
        ).apply(instance, FillingRecipe::new));
        public static final PacketCodec<RegistryByteBuf, FillingRecipe> PACKET_CODEC = PacketCodec.tuple(
            ItemStack.PACKET_CODEC,
            FillingRecipe::result,
            Ingredient.PACKET_CODEC,
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
        public PacketCodec<RegistryByteBuf, FillingRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
