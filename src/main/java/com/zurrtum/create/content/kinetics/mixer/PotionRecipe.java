package com.zurrtum.create.content.kinetics.mixer;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.content.processing.RecipeLoader;
import com.zurrtum.create.content.processing.basin.BasinInput;
import com.zurrtum.create.content.processing.basin.BasinRecipe;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.foundation.fluid.FluidPotionIngredient;
import com.zurrtum.create.infrastructure.component.BottleType;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryLoadTask.PendingRegistration;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static com.zurrtum.create.Create.MOD_ID;

public record PotionRecipe(FluidStack result, FluidIngredient fluidIngredient,
                           Ingredient ingredient) implements BasinRecipe {
    public static final MapCodec<PotionRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec((RecordCodecBuilder.Instance<PotionRecipe> instance) -> instance.group(
        FluidStack.CODEC.fieldOf("result").forGetter(PotionRecipe::result),
        FluidIngredient.CODEC.fieldOf("fluid_ingredient").forGetter(PotionRecipe::fluidIngredient),
        Ingredient.CODEC.fieldOf("ingredient").forGetter(PotionRecipe::ingredient)
    ).apply(instance, PotionRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, PotionRecipe> STREAM_CODEC = StreamCodec.composite(
        FluidStack.PACKET_CODEC,
        PotionRecipe::result,
        FluidIngredient.PACKET_CODEC,
        PotionRecipe::fluidIngredient,
        Ingredient.CONTENTS_STREAM_CODEC,
        PotionRecipe::ingredient,
        PotionRecipe::new
    );
    public static final RecipeSerializer<PotionRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public static void createFromBrewing(PendingRegistration<Recipe<?>> entry, BrewingRecipe recipe) {
        FluidIngredient fluidIngredient = FluidPotionIngredient.of(recipe.getInput(), 81000);
        if (fluidIngredient == null) {
            return;
        }
        Ingredient ingredient = recipe.getReagent().ingredient();
        ItemStackTemplate output = recipe.getOutput();
        BottleType type = PotionFluidHandler.bottleTypeFromItem(output.item().value());
        PotionContents potionContents = output.components().get(DataComponentMap.EMPTY, DataComponents.POTION_CONTENTS);
        if (potionContents == null) {
            potionContents = PotionContents.EMPTY;
        }
        FluidStack result = PotionFluidHandler.getFluidStack(81000, potionContents, type);
        RecipeLoader.addRecipe(
            Identifier.fromNamespaceAndPath(MOD_ID, entry.key().identifier().getPath()),
            new PotionRecipe(result, fluidIngredient, ingredient)
        );
    }

    @Override
    public int getIngredientSize() {
        return 2;
    }

    @Override
    public List<SizedIngredient> ingredients() {
        return List.of(new SizedIngredient(ingredient, 1));
    }

    @Override
    public List<FluidIngredient> fluidIngredients() {
        return List.of(fluidIngredient);
    }

    @Override
    public HeatCondition heat() {
        return HeatCondition.HEATED;
    }

    @Override
    public boolean matches(BasinInput input, Level world) {
        if (!HeatCondition.HEATED.testBlazeBurner(input.heat())) {
            return false;
        }
        ServerFilteringBehaviour filter = input.filter();
        if (filter == null) {
            return false;
        }
        if (!filter.test(result)) {
            return false;
        }
        List<ItemStack> outputs = BasinRecipe.tryCraft(input, ingredient);
        if (outputs == null) {
            return false;
        }
        if (!BasinRecipe.matchFluidIngredient(input, fluidIngredient)) {
            return false;
        }
        return input.acceptOutputs(outputs, List.of(result), true);
    }

    @Override
    public boolean apply(BasinInput input) {
        if (!HeatCondition.HEATED.testBlazeBurner(input.heat())) {
            return false;
        }
        Deque<Runnable> changes = new ArrayDeque<>();
        List<ItemStack> outputs = BasinRecipe.prepareCraft(input, ingredient, changes);
        if (outputs == null) {
            return false;
        }
        if (!BasinRecipe.prepareFluidCraft(input, fluidIngredient, changes)) {
            return false;
        }
        List<FluidStack> fluids = List.of(result);
        if (!input.acceptOutputs(outputs, fluids, true)) {
            return false;
        }
        changes.forEach(Runnable::run);
        return input.acceptOutputs(outputs, fluids, false);
    }

    @Override
    public RecipeSerializer<PotionRecipe> getSerializer() {
        return AllRecipeSerializers.POTION;
    }

    @Override
    public RecipeType<PotionRecipe> getType() {
        return AllRecipeTypes.POTION;
    }
}