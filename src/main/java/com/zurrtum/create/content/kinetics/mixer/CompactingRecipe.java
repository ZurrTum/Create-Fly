package com.zurrtum.create.content.kinetics.mixer;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.processing.basin.BasinInput;
import com.zurrtum.create.content.processing.basin.BasinRecipe;
import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

public record CompactingRecipe(
    ItemStack result, @Nullable FluidIngredient fluidIngredient, List<SizedIngredient> ingredients
) implements BasinRecipe {
    @Override
    public int getIngredientSize() {
        return (fluidIngredient == null ? 0 : 1) + ingredients.size();
    }

    @Override
    public List<SizedIngredient> getIngredients() {
        return ingredients;
    }

    @Override
    public List<FluidIngredient> getFluidIngredients() {
        return fluidIngredient == null ? List.of() : List.of(fluidIngredient);
    }

    @Override
    public boolean matches(BasinInput input, World world) {
        List<ItemStack> outputs = BasinRecipe.tryCraft(input, ingredients);
        if (outputs == null) {
            return false;
        }
        if (!BasinRecipe.matchFluidIngredient(input, fluidIngredient)) {
            return false;
        }
        outputs.add(result);
        return input.acceptOutputs(outputs, List.of(), true);
    }

    @Override
    public boolean apply(BasinInput input) {
        Deque<Runnable> changes = new ArrayDeque<>();
        List<ItemStack> outputs = BasinRecipe.prepareCraft(input, ingredients, changes);
        if (outputs == null) {
            return false;
        }
        if (!BasinRecipe.prepareFluidCraft(input, fluidIngredient, changes)) {
            return false;
        }
        outputs.add(result);
        if (!input.acceptOutputs(outputs, List.of(), true)) {
            return false;
        }
        changes.forEach(Runnable::run);
        return input.acceptOutputs(outputs, List.of(), false);
    }

    @Override
    public RecipeSerializer<CompactingRecipe> getSerializer() {
        return AllRecipeSerializers.COMPACTING;
    }

    @Override
    public RecipeType<CompactingRecipe> getType() {
        return AllRecipeTypes.COMPACTING;
    }

    public static class Serializer implements RecipeSerializer<CompactingRecipe> {
        public static final MapCodec<CompactingRecipe> CODEC = RecordCodecBuilder.mapCodec((Instance<CompactingRecipe> instance) -> instance.group(
            ItemStack.CODEC.fieldOf("result").forGetter(CompactingRecipe::result),
            FluidIngredient.CODEC.optionalFieldOf("fluid_ingredient").forGetter(Serializer::getOptionalFluidIngredient),
            SizedIngredient.getListCodec(1, 9).optionalFieldOf("ingredients", List.of()).forGetter(CompactingRecipe::ingredients)
        ).apply(instance, Serializer::createRecipe)).validate(recipe -> {
            if (recipe.fluidIngredient == null && recipe.ingredients.isEmpty()) {
                return DataResult.error(() -> "MixingRecipe must have a result or a fluid ingredient");
            }
            return DataResult.success(recipe);
        });
        public static final PacketCodec<RegistryByteBuf, CompactingRecipe> PACKET_CODEC = PacketCodec.tuple(
            ItemStack.PACKET_CODEC,
            CompactingRecipe::result,
            FluidIngredient.PACKET_CODEC.collect(PacketCodecs::optional),
            Serializer::getOptionalFluidIngredient,
            SizedIngredient.PACKET_CODEC.collect(PacketCodecs.toList()),
            CompactingRecipe::ingredients,
            Serializer::createRecipe
        );

        private static Optional<FluidIngredient> getOptionalFluidIngredient(CompactingRecipe recipe) {
            return Optional.ofNullable(recipe.fluidIngredient);
        }

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private static CompactingRecipe createRecipe(ItemStack result, Optional<FluidIngredient> fluidIngredient, List<SizedIngredient> ingredients) {
            return new CompactingRecipe(result, fluidIngredient.orElse(null), ingredients);
        }

        @Override
        public MapCodec<CompactingRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, CompactingRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
