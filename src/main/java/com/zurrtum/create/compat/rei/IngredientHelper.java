package com.zurrtum.create.compat.rei;

import com.google.common.base.Suppliers;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllFluids;
import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.foundation.fluid.FluidPotionIngredient;
import com.zurrtum.create.foundation.fluid.FluidStackIngredient;
import com.zurrtum.create.infrastructure.component.BottleType;
import dev.architectury.fluid.FluidStack;
import dev.architectury.utils.GameInstance;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.FabricIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface IngredientHelper {
    static EntryIngredient createEntryIngredient(com.zurrtum.create.infrastructure.fluids.FluidStack stack) {
        return EntryIngredients.of(FluidStack.create(stack.getFluid(), stack.getAmount(), stack.getComponentChanges()));
    }

    static EntryIngredient createEntryIngredient(FluidIngredient ingredient) {
        EntryDefinition<FluidStack> definition = VanillaEntryTypes.FLUID.getDefinition();
        int amount = ingredient.amount();
        if (ingredient instanceof FluidPotionIngredient potionIngredient) {
            Optional<HolderSet<Potion>> optional = potionIngredient.potions().potions();
            if (optional.isEmpty()) {
                return EntryIngredient.empty();
            }
            BottleType type = potionIngredient.type();
            HolderSet<Potion> holders = optional.get();
            EntryIngredient.Builder builder = EntryIngredient.builder(holders.size());
            for (Holder<Potion> holder : holders) {
                DataComponentPatch.Builder components = DataComponentPatch.builder();
                components.set(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, type);
                components.set(DataComponents.POTION_CONTENTS, new PotionContents(holder));
                FluidStack stack = FluidStack.create(AllFluids.POTION, amount, components.build());
                builder.add(EntryStack.of(definition, stack));
            }
            return builder.build();
        }
        List<Fluid> fluids = ingredient.getMatchingFluids();
        EntryIngredient.Builder builder = EntryIngredient.builder(fluids.size());
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

    static List<EntryIngredient> getFluidIngredientList(List<com.zurrtum.create.infrastructure.fluids.FluidStack> stacks) {
        return stacks.stream().map(IngredientHelper::createEntryIngredient).toList();
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
        Supplier<ContextMap> context = Suppliers.memoize(IngredientHelper::createIngredientContext);
        for (SizedIngredient sizedIngredient : ingredients) {
            results.add(getInputEntryIngredient(sizedIngredient, context));
        }
        return results.build();
    }

    static List<EntryIngredient> getEntryIngredients(Stream<EntryIngredient> first, Stream<EntryIngredient> second) {
        return Stream.concat(first, second).toList();
    }

    static ContextMap createIngredientContext() {
        MinecraftServer server = GameInstance.getServer();
        return ContextMap.builder().set(SlotDisplayContext.REGISTRIES, server.registryAccess())
            .buildAndValidate(SlotDisplayContext.CONTEXT);
    }

    static EntryIngredient getInputEntryIngredient(SizedIngredient sizedIngredient, Supplier<ContextMap> context) {
        int count = sizedIngredient.getCount();
        Ingredient ingredient = sizedIngredient.getIngredient();
        if (count == 1) {
            return getInputEntryIngredient(ingredient, context);
        }
        CustomIngredient customIngredient = ((FabricIngredient) ingredient).getCustomIngredient();
        List<ItemStack> stacks;
        if (customIngredient == null) {
            stacks = ingredient.values.stream().map(entry -> new ItemStack(entry, count)).toList();
        } else {
            stacks = customIngredient.display().resolveForStacks(context.get()).stream()
                .map(stack -> stack.copyWithCount(stack.getCount() * count)).toList();
        }
        return EntryIngredients.ofItemStacks(stacks);
    }

    static EntryIngredient getInputEntryIngredient(Ingredient ingredient, Supplier<ContextMap> context) {
        CustomIngredient customIngredient = ((FabricIngredient) ingredient).getCustomIngredient();
        if (customIngredient == null) {
            return EntryIngredients.ofIngredient(ingredient);
        }
        return EntryIngredients.ofItemStacks(customIngredient.display().resolveForStacks(context.get()));
    }

    static EntryIngredient getInputEntryIngredient(Ingredient ingredient) {
        return getInputEntryIngredient(ingredient, IngredientHelper::createIngredientContext);
    }
}
