package com.zurrtum.create.content.processing.basin;

import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public interface BasinRecipe extends Recipe<BasinInput> {
    Map<ShapelessRecipe, List<SizedIngredient>> SHAPELESS_CACHE = new IdentityHashMap<>();
    Map<ShapedRecipe, List<SizedIngredient>> SHAPED_CACHE = new IdentityHashMap<>();

    static boolean matchCraftingRecipe(BasinInput input, ShapelessRecipe recipe, World world) {
        return matchCraftingRecipe(input, recipe, world, SHAPELESS_CACHE, SizedIngredient::of);
    }

    static boolean matchCraftingRecipe(BasinInput input, ShapedRecipe recipe, World world) {
        return matchCraftingRecipe(input, recipe, world, SHAPED_CACHE, SizedIngredient::of);
    }

    private static <T extends CraftingRecipe> boolean matchCraftingRecipe(
        BasinInput input,
        T recipe,
        World world,
        Map<T, List<SizedIngredient>> ingredientCache,
        Function<T, List<SizedIngredient>> recipeToIngredients
    ) {
        ServerFilteringBehaviour filter = input.filter();
        if (filter == null) {
            return false;
        }
        ItemStack result = recipe.craft(null, world.getRegistryManager());
        if (!filter.test(result)) {
            return false;
        }
        List<SizedIngredient> ingredients = ingredientCache.computeIfAbsent(recipe, recipeToIngredients);
        if (ingredients.isEmpty()) {
            return false;
        }
        List<ItemStack> outputs = tryCraft(input, ingredients);
        if (outputs == null) {
            return false;
        }
        outputs.add(result);
        return input.acceptOutputs(outputs, List.of(), true);
    }

    @Nullable
    static List<ItemStack> tryCraft(BasinInput input, List<SizedIngredient> ingredients) {
        if (ingredients.isEmpty()) {
            return new ArrayList<>();
        }
        List<ItemStack> usings = new ArrayList<>();
        List<ItemStack> inputs = new LinkedList<>();
        int ingredientIndex = 0;
        int ingredientSize = ingredients.size();
        Inventory inventory = input.items();
        Find:
        for (int itemIndex = 0, inventorySize = inventory.size(); ingredientIndex < ingredientSize; ingredientIndex++) {
            SizedIngredient ingredient = ingredients.get(ingredientIndex);
            int size = inputs.size();
            int remainder = ingredient.getCount();
            for (; itemIndex < inventorySize; itemIndex++) {
                ItemStack stack = inventory.getStack(itemIndex);
                if (stack.isEmpty()) {
                    continue;
                }
                if (ingredient.test(stack)) {
                    int count = stack.getCount();
                    if (count > remainder) {
                        usings.add(stack.copyWithCount(remainder));
                        itemIndex++;
                        continue Find;
                    } else {
                        usings.add(stack);
                        if (count == remainder) {
                            itemIndex++;
                            continue Find;
                        }
                        remainder -= count;
                    }
                } else {
                    inputs.add(stack);
                }
            }
            Iterator<ItemStack> iterator = inputs.subList(0, size).iterator();
            while (iterator.hasNext()) {
                ItemStack stack = iterator.next();
                if (ingredient.test(stack)) {
                    iterator.remove();
                    int count = stack.getCount();
                    if (count > remainder) {
                        usings.add(stack.copyWithCount(remainder));
                        ingredientIndex++;
                        break Find;
                    } else {
                        usings.add(stack);
                        if (count == remainder) {
                            ingredientIndex++;
                            break Find;
                        }
                        remainder -= count;
                    }
                }
            }
            return null;
        }
        Find:
        for (; ingredientIndex < ingredientSize; ingredientIndex++) {
            SizedIngredient ingredient = ingredients.get(ingredientIndex);
            int remainder = ingredient.getCount();
            Iterator<ItemStack> iterator = inputs.iterator();
            while (iterator.hasNext()) {
                ItemStack stack = iterator.next();
                if (ingredient.test(stack)) {
                    iterator.remove();
                    int count = stack.getCount();
                    if (count > remainder) {
                        usings.add(stack.copyWithCount(remainder));
                        continue Find;
                    } else {
                        usings.add(stack);
                        if (count == remainder) {
                            continue Find;
                        }
                        remainder -= count;
                    }
                }
            }
            return null;
        }
        List<ItemStack> outputs = new ArrayList<>();
        for (ItemStack stack : usings) {
            Item item = stack.getItem();
            for (int i = 0, count = stack.getCount(); i < count; i++) {
                ItemStack remainder = item.getRecipeRemainder();
                if (remainder != ItemStack.EMPTY) {
                    outputs.add(remainder);
                }
            }
        }
        return outputs;
    }

    static boolean matchFluidIngredient(BasinInput input, @Nullable FluidIngredient ingredient) {
        if (ingredient == null) {
            return false;
        }
        int remainder = ingredient.amount();
        for (FluidStack stack : input.fluids()) {
            if (ingredient.test(stack)) {
                int amount = stack.getAmount();
                if (amount >= remainder) {
                    return true;
                }
                remainder -= amount;
            }
        }
        return false;
    }

    static boolean matchFluidIngredient(BasinInput input, List<FluidIngredient> ingredients) {
        if (ingredients.isEmpty()) {
            return true;
        }
        List<FluidStack> inputs = new LinkedList<>();
        int ingredientIndex = 0;
        int ingredientSize = ingredients.size();
        FluidInventory inventory = input.fluids();
        Find:
        for (int fluidIndex = 0, inventorySize = inventory.size(); ingredientIndex < ingredientSize; ingredientIndex++) {
            FluidIngredient ingredient = ingredients.get(ingredientIndex);
            int size = inputs.size();
            int remainder = ingredient.amount();
            for (; fluidIndex < inventorySize; fluidIndex++) {
                FluidStack stack = inventory.getStack(fluidIndex);
                if (stack.isEmpty()) {
                    continue;
                }
                if (ingredient.test(stack)) {
                    int amount = stack.getAmount();
                    if (amount >= remainder) {
                        fluidIndex++;
                        continue Find;
                    } else {
                        remainder -= amount;
                    }
                } else {
                    inputs.add(stack);
                }
            }
            Iterator<FluidStack> iterator = inputs.subList(0, size).iterator();
            while (iterator.hasNext()) {
                FluidStack stack = iterator.next();
                if (ingredient.test(stack)) {
                    iterator.remove();
                    int count = stack.getAmount();
                    if (count >= remainder) {
                        ingredientIndex++;
                        break Find;
                    } else {
                        remainder -= count;
                    }
                }
            }
            return false;
        }
        Find:
        for (; ingredientIndex < ingredientSize; ingredientIndex++) {
            FluidIngredient ingredient = ingredients.get(ingredientIndex);
            int remainder = ingredient.amount();
            Iterator<FluidStack> iterator = inputs.iterator();
            while (iterator.hasNext()) {
                FluidStack stack = iterator.next();
                if (ingredient.test(stack)) {
                    iterator.remove();
                    int count = stack.getAmount();
                    if (count >= remainder) {
                        continue Find;
                    } else {
                        remainder -= count;
                    }
                }
            }
            return false;
        }
        return true;
    }

    static boolean applyCraftingRecipe(BasinInput input, ShapedRecipe recipe, World world) {
        return applyCraftingRecipe(input, recipe, world, SHAPED_CACHE, SizedIngredient::of);
    }

    static boolean applyCraftingRecipe(BasinInput input, ShapelessRecipe recipe, World world) {
        return applyCraftingRecipe(input, recipe, world, SHAPELESS_CACHE, SizedIngredient::of);
    }

    private static <T extends CraftingRecipe> boolean applyCraftingRecipe(
        BasinInput input,
        T recipe,
        World world,
        Map<T, List<SizedIngredient>> ingredientCache,
        Function<T, List<SizedIngredient>> recipeToIngredients
    ) {
        List<SizedIngredient> ingredients = ingredientCache.computeIfAbsent(recipe, recipeToIngredients);
        Deque<Runnable> changes = new ArrayDeque<>();
        List<ItemStack> outputs = prepareCraft(input, ingredients, changes);
        if (outputs == null) {
            return false;
        }
        outputs.add(recipe.craft(null, world.getRegistryManager()));
        if (!input.acceptOutputs(outputs, List.of(), true)) {
            return false;
        }
        changes.forEach(Runnable::run);
        return input.acceptOutputs(outputs, List.of(), false);
    }

    @Nullable
    static List<ItemStack> prepareCraft(BasinInput input, List<SizedIngredient> ingredients, Deque<Runnable> changes) {
        if (ingredients.isEmpty()) {
            return new ArrayList<>();
        }
        List<ItemStack> usings = new ArrayList<>();
        List<IntObjectPair<ItemStack>> inputs = new LinkedList<>();
        int ingredientIndex = 0;
        int ingredientSize = ingredients.size();
        Inventory inventory = input.items();
        Apply:
        for (int itemIndex = 0, inventorySize = inventory.size(); ingredientIndex < ingredientSize; ingredientIndex++) {
            SizedIngredient ingredient = ingredients.get(ingredientIndex);
            int size = inputs.size();
            int remainder = ingredient.getCount();
            for (; itemIndex < inventorySize; itemIndex++) {
                ItemStack stack = inventory.getStack(itemIndex);
                if (stack.isEmpty()) {
                    continue;
                }
                if (ingredient.test(stack)) {
                    int count = stack.getCount();
                    if (count > remainder) {
                        usings.add(stack.copyWithCount(remainder));
                        int newCount = count - remainder;
                        changes.add(() -> stack.setCount(newCount));
                        itemIndex++;
                        continue Apply;
                    } else {
                        usings.add(stack);
                        int slot = itemIndex;
                        changes.add(() -> inventory.setStack(slot, ItemStack.EMPTY));
                        if (count == remainder) {
                            itemIndex++;
                            continue Apply;
                        } else {
                            remainder -= count;
                        }
                    }
                } else {
                    inputs.add(IntObjectPair.of(itemIndex, stack));
                }
            }
            Iterator<IntObjectPair<ItemStack>> iterator = inputs.subList(0, size).iterator();
            while (iterator.hasNext()) {
                IntObjectPair<ItemStack> pair = iterator.next();
                ItemStack stack = pair.right();
                if (ingredient.test(stack)) {
                    iterator.remove();
                    int count = stack.getCount();
                    if (count > remainder) {
                        usings.add(stack.copyWithCount(remainder));
                        int newCount = count - remainder;
                        changes.add(() -> stack.setCount(newCount));
                        ingredientIndex++;
                        break Apply;
                    } else {
                        usings.add(stack);
                        int slot = pair.leftInt();
                        changes.add(() -> inventory.setStack(slot, ItemStack.EMPTY));
                        if (count == remainder) {
                            ingredientIndex++;
                            break Apply;
                        } else {
                            remainder -= count;
                        }
                    }
                }
            }
            return null;
        }
        Apply:
        for (; ingredientIndex < ingredientSize; ingredientIndex++) {
            SizedIngredient ingredient = ingredients.get(ingredientIndex);
            int remainder = ingredient.getCount();
            Iterator<IntObjectPair<ItemStack>> iterator = inputs.iterator();
            while (iterator.hasNext()) {
                IntObjectPair<ItemStack> pair = iterator.next();
                ItemStack stack = pair.right();
                if (ingredient.test(stack)) {
                    iterator.remove();
                    int count = stack.getCount();
                    if (count > remainder) {
                        usings.add(stack.copyWithCount(remainder));
                        int newCount = count - remainder;
                        changes.add(() -> stack.setCount(newCount));
                        continue Apply;
                    } else {
                        usings.add(stack);
                        int slot = pair.leftInt();
                        changes.add(() -> inventory.setStack(slot, ItemStack.EMPTY));
                        if (count == remainder) {
                            continue Apply;
                        } else {
                            remainder -= count;
                        }
                    }
                }
            }
            return null;
        }
        changes.add(inventory::markDirty);
        List<ItemStack> outputs = new ArrayList<>();
        for (ItemStack stack : usings) {
            Item item = stack.getItem();
            for (int i = 0, count = stack.getCount(); i < count; i++) {
                ItemStack remainder = item.getRecipeRemainder();
                if (remainder != ItemStack.EMPTY) {
                    outputs.add(remainder);
                }
            }
        }
        return outputs;
    }

    static boolean prepareFluidCraft(BasinInput input, FluidIngredient ingredient, Deque<Runnable> changes) {
        if (ingredient == null) {
            return true;
        }
        FluidInventory inventory = input.fluids();
        int remainder = ingredient.amount();
        int fluidInventorySize = inventory.size();
        for (int fluidIndex = 0; fluidIndex < fluidInventorySize; fluidIndex++) {
            FluidStack stack = inventory.getStack(fluidIndex);
            if (ingredient.test(stack)) {
                int amount = stack.getAmount();
                if (amount > remainder) {
                    int newAmount = amount - remainder;
                    changes.add(() -> stack.setAmount(newAmount));
                    return true;
                } else {
                    int slot = fluidIndex;
                    changes.add(() -> inventory.setStack(slot, FluidStack.EMPTY));
                    if (remainder == amount) {
                        return true;
                    }
                    remainder -= amount;
                }
            }
        }
        return false;
    }

    static boolean prepareFluidCraft(BasinInput input, List<FluidIngredient> ingredients, Deque<Runnable> changes) {
        if (ingredients.isEmpty()) {
            return true;
        }
        List<IntObjectPair<FluidStack>> inputs = new LinkedList<>();
        int ingredientIndex = 0;
        int ingredientSize = ingredients.size();
        FluidInventory inventory = input.fluids();
        Apply:
        for (int fluidIndex = 0, inventorySize = inventory.size(); ingredientIndex < ingredientSize; ingredientIndex++) {
            FluidIngredient ingredient = ingredients.get(ingredientIndex);
            int size = inputs.size();
            int remainder = ingredient.amount();
            for (; fluidIndex < inventorySize; fluidIndex++) {
                FluidStack stack = inventory.getStack(fluidIndex);
                if (stack.isEmpty()) {
                    continue;
                }
                if (ingredient.test(stack)) {
                    int count = stack.getAmount();
                    if (count > remainder) {
                        int newAmount = count - remainder;
                        changes.add(() -> stack.setAmount(newAmount));
                        fluidIndex++;
                        continue Apply;
                    } else {
                        int slot = fluidIndex;
                        changes.add(() -> inventory.setStack(slot, FluidStack.EMPTY));
                        if (count == remainder) {
                            fluidIndex++;
                            continue Apply;
                        } else {
                            remainder -= count;
                        }
                    }
                } else {
                    inputs.add(IntObjectPair.of(fluidIndex, stack));
                }
            }
            Iterator<IntObjectPair<FluidStack>> iterator = inputs.subList(0, size).iterator();
            while (iterator.hasNext()) {
                IntObjectPair<FluidStack> pair = iterator.next();
                FluidStack stack = pair.right();
                if (ingredient.test(stack)) {
                    iterator.remove();
                    int count = stack.getAmount();
                    if (count > remainder) {
                        int newAmount = count - remainder;
                        changes.add(() -> stack.setAmount(newAmount));
                        ingredientIndex++;
                        break Apply;
                    } else {
                        int slot = pair.leftInt();
                        changes.add(() -> inventory.setStack(slot, FluidStack.EMPTY));
                        if (count == remainder) {
                            ingredientIndex++;
                            break Apply;
                        } else {
                            remainder -= count;
                        }
                    }
                }
            }
            return false;
        }
        Apply:
        for (; ingredientIndex < ingredientSize; ingredientIndex++) {
            FluidIngredient ingredient = ingredients.get(ingredientIndex);
            int remainder = ingredient.amount();
            Iterator<IntObjectPair<FluidStack>> iterator = inputs.iterator();
            while (iterator.hasNext()) {
                IntObjectPair<FluidStack> pair = iterator.next();
                FluidStack stack = pair.right();
                if (ingredient.test(stack)) {
                    iterator.remove();
                    int count = stack.getAmount();
                    if (count > remainder) {
                        int newAmount = count - remainder;
                        changes.add(() -> stack.setAmount(newAmount));
                        continue Apply;
                    } else {
                        int slot = pair.leftInt();
                        changes.add(() -> inventory.setStack(slot, FluidStack.EMPTY));
                        if (count == remainder) {
                            continue Apply;
                        } else {
                            remainder -= count;
                        }
                    }
                }
            }
            return false;
        }
        return true;
    }

    int getIngredientSize();

    boolean apply(BasinInput input);

    @Override
    default IngredientPlacement getIngredientPlacement() {
        return IngredientPlacement.NONE;
    }

    @Override
    default RecipeBookCategory getRecipeBookCategory() {
        return null;
    }

    @Override
    default boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    default ItemStack craft(BasinInput input, RegistryWrapper.WrapperLookup registries) {
        return ItemStack.EMPTY;
    }
}
