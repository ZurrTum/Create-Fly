package com.zurrtum.create.compat.rei;

import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.foundation.fluid.FluidStackIngredient;
import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.FabricIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.ComponentsIngredient;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public interface IngredientHelper {
    static EntryIngredient createEntryIngredient(com.zurrtum.create.infrastructure.fluids.FluidStack stack) {
        return EntryIngredients.of(FluidStack.create(stack.getFluid(), stack.getAmount(), stack.getComponentChanges()));
    }

    static EntryIngredient createEntryIngredient(FluidIngredient ingredient) {
        EntryDefinition<FluidStack> definition = VanillaEntryTypes.FLUID.getDefinition();
        List<Fluid> fluids = ingredient.getMatchingFluids();
        EntryIngredient.Builder builder = EntryIngredient.builder(fluids.size());
        int amount = ingredient.amount();
        DataComponentPatch patch = DataComponentPatch.EMPTY;
        if (ingredient instanceof FluidStackIngredient stackIngredient) {
            patch = stackIngredient.components();
        }
        for (Fluid fluid : fluids) {
            FluidStack stack = FluidStack.create(fluid, amount, patch);
            builder.add(EntryStack.of(definition, stack));
        }
        return builder.build();
    }

    static Stream<EntryIngredient> getFluidIngredientStream(@Nullable FluidIngredient ingredient) {
        return ingredient == null ? Stream.empty() : Stream.of(createEntryIngredient(ingredient));
    }

    static Stream<EntryIngredient> getFluidIngredientStream(List<FluidIngredient> ingredients) {
        if (ingredients.isEmpty()) {
            return Stream.empty();
        }
        Stream.Builder<EntryIngredient> builder = Stream.builder();
        for (FluidIngredient ingredient : ingredients) {
            builder.add(createEntryIngredient(ingredient));
        }
        return builder.build();
    }

    static Stream<EntryIngredient> getSizedIngredientStream(List<SizedIngredient> ingredients) {
        Stream.Builder<EntryIngredient> results = Stream.builder();
        EntryDefinition<ItemStack> definition = VanillaEntryTypes.ITEM.getDefinition();
        int size = ingredients.size();
        for (SizedIngredient ingredient : ingredients) {
            EntryIngredient.Builder builder = EntryIngredient.builder(size);
            ingredient.getIngredient().values.forEach(stack -> {
                builder.add(EntryStack.of(definition, new ItemStack(stack, ingredient.getCount())));
            });
            results.add(builder.build());
        }
        return results.build();
    }

    static List<EntryIngredient> getEntryIngredients(Stream<EntryIngredient> first, Stream<EntryIngredient> second) {
        return Stream.concat(first, second).toList();
    }

    @SuppressWarnings("UnstableApiUsage")
    static EntryIngredient getInputEntryIngredient(Ingredient ingredient) {
        CustomIngredient customIngredient = ((FabricIngredient) ingredient).getCustomIngredient();
        if (customIngredient instanceof ComponentsIngredient) {
            EntryDefinition<ItemStack> definition = VanillaEntryTypes.ITEM.getDefinition();
            List<SlotDisplay> contents = ((SlotDisplay.Composite) customIngredient.toDisplay()).contents();
            EntryIngredient.Builder builder = EntryIngredient.builder(contents.size());
            for (SlotDisplay content : contents) {
                SlotDisplay.ItemStackSlotDisplay display = (SlotDisplay.ItemStackSlotDisplay) content;
                builder.add(EntryStack.of(definition, display.stack()));
            }
            return builder.build();
        } else {
            return EntryIngredients.ofIngredient(ingredient);
        }
    }
}
