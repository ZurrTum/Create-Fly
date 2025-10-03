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
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.infrastructure.component.BottleType;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.*;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.*;

import static com.zurrtum.create.Create.MOD_ID;

public record PotionRecipe(FluidStack result, FluidIngredient fluidIngredient, Ingredient ingredient) implements BasinRecipe {
    public static final List<Item> SUPPORTED_CONTAINERS = List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);
    public static ReloadData data;

    public static void register(Map<Identifier, Recipe<?>> map) {
        if (data == null) {
            return;
        }
        BrewingRecipeRegistry potionBrewing = BrewingRecipeRegistry.create(data.enabledFeatures);
        int recipeIndex = 0;
        List<Item> allowedSupportedContainers = new ArrayList<>();
        for (Item container : SUPPORTED_CONTAINERS) {
            if (potionBrewing.isPotionType(new ItemStack(container))) {
                allowedSupportedContainers.add(container);
            }
        }
        for (Item container : allowedSupportedContainers) {
            BottleType bottleType = PotionFluidHandler.bottleTypeFromItem(container);
            for (BrewingRecipeRegistry.Recipe<Potion> mix : potionBrewing.potionRecipes) {
                FluidIngredient fromFluid = PotionFluidHandler.getFluidIngredientFromPotion(
                    new PotionContentsComponent(mix.from()),
                    bottleType,
                    81000
                );
                FluidStack toFluid = PotionFluidHandler.getFluidFromPotion(new PotionContentsComponent(mix.to()), bottleType, 81000);
                Identifier id = Identifier.of(MOD_ID, "potion_mixing_vanilla_" + recipeIndex++);
                map.put(id, new PotionRecipe(toFluid, fromFluid, mix.ingredient()));
            }
        }
        for (BrewingRecipeRegistry.Recipe<Item> mix : potionBrewing.itemRecipes) {
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

            List<Reference<Potion>> potions = data.registries.getOrThrow(RegistryKeys.POTION).streamEntries().toList();

            for (Reference<Potion> potion : potions) {
                FluidIngredient fromFluid = PotionFluidHandler.getFluidIngredientFromPotion(
                    new PotionContentsComponent(potion),
                    fromBottleType,
                    81000
                );
                FluidStack toFluid = PotionFluidHandler.getFluidFromPotion(new PotionContentsComponent(potion), toBottleType, 81000);
                Identifier id = Identifier.of(MOD_ID, "potion_mixing_vanilla_" + recipeIndex++);
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
    public boolean matches(BasinInput input, World world) {
        if (!HeatCondition.HEATED.testBlazeBurner(input.heat())) {
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
        RegistryWrapper.WrapperLookup registries, FeatureSet enabledFeatures
    ) {
    }

    public static class Serializer implements RecipeSerializer<PotionRecipe> {
        public static final MapCodec<PotionRecipe> CODEC = RecordCodecBuilder.mapCodec((RecordCodecBuilder.Instance<PotionRecipe> instance) -> instance.group(
            FluidStack.CODEC.fieldOf("result").forGetter(PotionRecipe::result),
            FluidIngredient.CODEC.fieldOf("fluid_ingredient").forGetter(PotionRecipe::fluidIngredient),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(PotionRecipe::ingredient)
        ).apply(instance, PotionRecipe::new));
        public static final PacketCodec<RegistryByteBuf, PotionRecipe> PACKET_CODEC = PacketCodec.tuple(
            FluidStack.PACKET_CODEC,
            PotionRecipe::result,
            FluidIngredient.PACKET_CODEC,
            PotionRecipe::fluidIngredient,
            Ingredient.PACKET_CODEC,
            PotionRecipe::ingredient,
            PotionRecipe::new
        );

        @Override
        public MapCodec<PotionRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, PotionRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}