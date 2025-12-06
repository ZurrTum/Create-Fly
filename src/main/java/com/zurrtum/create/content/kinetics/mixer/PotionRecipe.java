package com.zurrtum.create.content.kinetics.mixer;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.content.processing.basin.BasinInput;
import com.zurrtum.create.content.processing.basin.BasinRecipe;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.infrastructure.component.BottleType;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.*;

import static com.zurrtum.create.Create.MOD_ID;

public record PotionRecipe(FluidStack result, FluidIngredient fluidIngredient, Ingredient ingredient) implements BasinRecipe {
    public static final List<Item> SUPPORTED_CONTAINERS = List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);
    public static ReloadData data;

    public static void register(Map<Identifier, Recipe<?>> map) {
        if (data == null) {
            return;
        }
        PotionBrewing potionBrewing = PotionBrewing.bootstrap(data.enabledFeatures);
        int recipeIndex = 0;
        List<Item> allowedSupportedContainers = new ArrayList<>();
        for (Item container : SUPPORTED_CONTAINERS) {
            if (potionBrewing.isContainer(new ItemStack(container))) {
                allowedSupportedContainers.add(container);
            }
        }
        for (Item container : allowedSupportedContainers) {
            BottleType bottleType = PotionFluidHandler.bottleTypeFromItem(container);
            for (PotionBrewing.Mix<Potion> mix : potionBrewing.potionMixes) {
                FluidIngredient fromFluid = PotionFluidHandler.getFluidIngredientFromPotion(new PotionContents(mix.from()), bottleType, 81000);
                FluidStack toFluid = PotionFluidHandler.getFluidFromPotion(new PotionContents(mix.to()), bottleType, 81000);
                Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, "potion_mixing_vanilla_" + recipeIndex++);
                map.put(id, new PotionRecipe(toFluid, fromFluid, mix.ingredient()));
            }
        }
        for (PotionBrewing.Mix<Item> mix : potionBrewing.containerMixes) {
            Item from = mix.from().value();
            if (!allowedSupportedContainers.contains(from)) {
                continue;
            }
            Item to = mix.to().value();
            if (!allowedSupportedContainers.contains(to)) {
                continue;
            }
            BottleType fromBottleType = PotionFluidHandler.bottleTypeFromItem(from);
            BottleType toBottleType = PotionFluidHandler.bottleTypeFromItem(to);
            Ingredient ingredient = mix.ingredient();

            List<Reference<Potion>> potions = data.registries.lookupOrThrow(Registries.POTION).listElements().toList();

            for (Reference<Potion> potion : potions) {
                FluidIngredient fromFluid = PotionFluidHandler.getFluidIngredientFromPotion(new PotionContents(potion), fromBottleType, 81000);
                FluidStack toFluid = PotionFluidHandler.getFluidFromPotion(new PotionContents(potion), toBottleType, 81000);
                Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, "potion_mixing_vanilla_" + recipeIndex++);
                map.put(id, new PotionRecipe(toFluid, fromFluid, ingredient));
            }
        }
        data = null;
    }

    @Override
    public int getIngredientSize() {
        return 2;
    }

    @Override
    public List<SizedIngredient> getIngredients() {
        return List.of(new SizedIngredient(ingredient, 1));
    }

    @Override
    public List<FluidIngredient> getFluidIngredients() {
        return List.of(fluidIngredient);
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

    public record ReloadData(
        HolderLookup.Provider registries, FeatureFlagSet enabledFeatures
    ) {
    }

    public static class Serializer implements RecipeSerializer<PotionRecipe> {
        public static final MapCodec<PotionRecipe> CODEC = RecordCodecBuilder.mapCodec((RecordCodecBuilder.Instance<PotionRecipe> instance) -> instance.group(
            FluidStack.CODEC.fieldOf("result").forGetter(PotionRecipe::result),
            FluidIngredient.CODEC.fieldOf("fluid_ingredient").forGetter(PotionRecipe::fluidIngredient),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(PotionRecipe::ingredient)
        ).apply(instance, PotionRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, PotionRecipe> PACKET_CODEC = StreamCodec.composite(
            FluidStack.PACKET_CODEC,
            PotionRecipe::result,
            FluidIngredient.PACKET_CODEC,
            PotionRecipe::fluidIngredient,
            Ingredient.CONTENTS_STREAM_CODEC,
            PotionRecipe::ingredient,
            PotionRecipe::new
        );

        @Override
        public MapCodec<PotionRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, PotionRecipe> streamCodec() {
            return PACKET_CODEC;
        }
    }
}