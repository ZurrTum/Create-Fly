package com.zurrtum.create.compat.rei;

import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public interface IngredientHelper {
    static Stream<EntryIngredient> getEntryIngredient(@Nullable FluidIngredient ingredient) {
        if (ingredient == null) {
            return Stream.empty();
        }
        EntryDefinition<FluidStack> definition = VanillaEntryTypes.FLUID.getDefinition();
        List<Fluid> fluids = ingredient.getMatchingFluids();
        EntryIngredient.Builder builder = EntryIngredient.builder(fluids.size());
        int amount = ingredient.amount();
        for (Fluid fluid : fluids) {
            FluidStack stack = FluidStack.create(fluid, amount);
            builder.add(EntryStack.of(definition, stack));
        }
        return Stream.of(builder.build());
    }

    static Stream<EntryIngredient> getEntryIngredients(List<SizedIngredient> ingredients) {
        Stream.Builder<EntryIngredient> results = Stream.builder();
        EntryDefinition<ItemStack> definition = VanillaEntryTypes.ITEM.getDefinition();
        int size = ingredients.size();
        for (SizedIngredient ingredient : ingredients) {
            EntryIngredient.Builder builder = EntryIngredient.builder(size);
            ingredient.getIngredient().entries.forEach(stack -> {
                builder.add(EntryStack.of(definition, new ItemStack(stack, ingredient.getCount())));
            });
            results.add(builder.build());
        }
        return results.build();
    }

    static List<EntryIngredient> getEntryIngredients(Stream<EntryIngredient> ingredients, Stream<EntryIngredient> fluidIngredients) {
        return Stream.concat(ingredients, fluidIngredients).toList();
    }
}
