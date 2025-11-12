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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

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
    public boolean matches(BasinInput input, Level world) {
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
                return DataResult.error(() -> "MixingRecipe must have a ingredient or a fluid ingredient");
            }
            return DataResult.success(recipe);
        });
        public static final StreamCodec<RegistryFriendlyByteBuf, CompactingRecipe> PACKET_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC,
            CompactingRecipe::result,
            FluidIngredient.PACKET_CODEC.apply(ByteBufCodecs::optional),
            Serializer::getOptionalFluidIngredient,
            SizedIngredient.PACKET_CODEC.apply(ByteBufCodecs.list()),
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
        public StreamCodec<RegistryFriendlyByteBuf, CompactingRecipe> streamCodec() {
            return PACKET_CODEC;
        }
    }
}
