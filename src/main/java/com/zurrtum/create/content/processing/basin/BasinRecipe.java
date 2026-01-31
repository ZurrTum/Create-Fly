package com.zurrtum.create.content.processing.basin;

import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.foundation.recipe.CreateRecipe;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public interface BasinRecipe extends CreateRecipe<BasinInput> {
    Map<ShapelessRecipe, List<SizedIngredient>> SHAPELESS_CACHE = new IdentityHashMap<>();
    Map<ShapedRecipe, List<SizedIngredient>> SHAPED_CACHE = new IdentityHashMap<>();

    static boolean matchCraftingRecipe(BasinInput input, ShapelessRecipe recipe, Level world) {
        return matchCraftingRecipe(input, recipe, world, SHAPELESS_CACHE, SizedIngredient::of);
    }

    static boolean matchCraftingRecipe(BasinInput input, ShapedRecipe recipe, Level world) {
        return matchCraftingRecipe(input, recipe, world, SHAPED_CACHE, SizedIngredient::of);
    }

    private static <T extends CraftingRecipe> boolean matchCraftingRecipe(
        BasinInput input,
        T recipe,
        Level world,
        Map<T, List<SizedIngredient>> ingredientCache,
        Function<T, List<SizedIngredient>> recipeToIngredients
    ) {
        ServerFilteringBehaviour filter = input.filter();
        if (filter == null) {
            return false;
        }
        ItemStack result;
        try {
            result = recipe.assemble(CraftingInput.EMPTY);
        } catch (Exception ignore) {
            return false;
        }
        if (result.isEmpty() || !filter.test(result)) {
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
    static List<ItemStack> tryCraft(BasinInput input, Ingredient ingredient) {
        Container inventory = input.items();
        for (int i = 0, size = inventory.getContainerSize(); i < size; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (ingredient.test(stack)) {
                List<ItemStack> outputs = new ArrayList<>();
                ItemStackTemplate remainder = stack.getItem().getCraftingRemainder();
                if (remainder != null) {
                    outputs.add(remainder.create());
                }
                return outputs;
            }
        }
        return null;
    }

    @Nullable
    static List<ItemStack> tryCraft(BasinInput input, SizedIngredient ingredient) {
        int remainder = ingredient.getCount();
        if (remainder == 1) {
            return tryCraft(input, ingredient.getIngredient());
        }
        Container inventory = input.items();
        List<ItemStack> outputs = new ArrayList<>();
        for (int i = 0, size = inventory.getContainerSize(); i < size; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (ingredient.test(stack)) {
                int extract = Math.min(stack.getCount(), remainder);
                addRecipeRemainder(stack, extract, outputs);
                if (extract == remainder) {
                    return outputs;
                }
                remainder -= extract;
            }
        }
        return null;
    }

    @Nullable
    static List<ItemStack> tryCraft(BasinInput input, List<SizedIngredient> ingredients) {
        int ingredientSize = ingredients.size();
        if (ingredientSize == 0) {
            return new ArrayList<>();
        }
        if (ingredientSize == 1) {
            return tryCraft(input, ingredients.getFirst());
        }
        List<ItemStack> usings = new ArrayList<>();
        List<ItemStack> inputs = new LinkedList<>();
        int ingredientIndex = 0;
        Container inventory = input.items();
        Find:
        for (int itemIndex = 0, inventorySize = inventory.getContainerSize(); ingredientIndex < ingredientSize; ingredientIndex++) {
            SizedIngredient ingredient = ingredients.get(ingredientIndex);
            int size = inputs.size();
            int remainder = ingredient.getCount();
            for (; itemIndex < inventorySize; itemIndex++) {
                ItemStack stack = inventory.getItem(itemIndex);
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
            addRecipeRemainder(stack, stack.getCount(), outputs);
        }
        return outputs;
    }

    static boolean matchFluidIngredient(BasinInput input, @Nullable FluidIngredient ingredient) {
        if (ingredient == null) {
            return true;
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
        int ingredientSize = ingredients.size();
        if (ingredientSize == 0) {
            return true;
        }
        if (ingredientSize == 1) {
            return matchFluidIngredient(input, ingredients.getFirst());
        }
        List<FluidStack> inputs = new LinkedList<>();
        int ingredientIndex = 0;
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

    static boolean applyCraftingRecipe(BasinInput input, ShapedRecipe recipe, Level world) {
        return applyCraftingRecipe(input, recipe, world, SHAPED_CACHE, SizedIngredient::of);
    }

    static boolean applyCraftingRecipe(BasinInput input, ShapelessRecipe recipe, Level world) {
        return applyCraftingRecipe(input, recipe, world, SHAPELESS_CACHE, SizedIngredient::of);
    }

    private static <T extends CraftingRecipe> boolean applyCraftingRecipe(
        BasinInput input,
        T recipe,
        Level world,
        Map<T, List<SizedIngredient>> ingredientCache,
        Function<T, List<SizedIngredient>> recipeToIngredients
    ) {
        List<SizedIngredient> ingredients = ingredientCache.computeIfAbsent(recipe, recipeToIngredients);
        Deque<Runnable> changes = new ArrayDeque<>();
        List<ItemStack> outputs = prepareCraft(input, ingredients, changes);
        if (outputs == null) {
            return false;
        }
        outputs.add(recipe.assemble(CraftingInput.EMPTY));
        if (!input.acceptOutputs(outputs, List.of(), true)) {
            return false;
        }
        changes.forEach(Runnable::run);
        return input.acceptOutputs(outputs, List.of(), false);
    }

    static void addRecipeRemainder(ItemStack stack, int count, List<ItemStack> outputs) {
        Item item = stack.getItem();
        for (int i = 0; i < count; i++) {
            ItemStackTemplate remainder = item.getCraftingRemainder();
            if (remainder != null) {
                outputs.add(remainder.create());
            }
        }
    }

    @Nullable
    static List<ItemStack> prepareCraft(BasinInput input, Ingredient ingredient, Deque<Runnable> changes) {
        Container inventory = input.items();
        for (int i = 0, size = inventory.getContainerSize(); i < size; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (ingredient.test(stack)) {
                int count = stack.getCount();
                if (count > 1) {
                    int newCount = count - 1;
                    changes.add(() -> {
                        stack.setCount(newCount);
                        inventory.setChanged();
                    });
                } else {
                    int slot = i;
                    changes.add(() -> {
                        inventory.setItem(slot, ItemStack.EMPTY);
                        inventory.setChanged();
                    });
                }
                List<ItemStack> outputs = new ArrayList<>();
                ItemStackTemplate remainder = stack.getItem().getCraftingRemainder();
                if (remainder != null) {
                    outputs.add(remainder.create());
                }
                return outputs;
            }
        }
        return null;
    }

    @Nullable
    static List<ItemStack> prepareCraft(BasinInput input, SizedIngredient ingredient, Deque<Runnable> changes) {
        int remainder = ingredient.getCount();
        if (remainder == 1) {
            return prepareCraft(input, ingredient.getIngredient(), changes);
        }
        Container inventory = input.items();
        List<ItemStack> outputs = new ArrayList<>();
        for (int i = 0, size = inventory.getContainerSize(); i < size; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (ingredient.test(stack)) {
                int count = stack.getCount();
                int using;
                if (count > remainder) {
                    int newCount = count - remainder;
                    changes.add(() -> stack.setCount(newCount));
                    using = remainder;
                } else {
                    int slot = i;
                    changes.add(() -> inventory.setItem(slot, ItemStack.EMPTY));
                    using = count;
                }
                addRecipeRemainder(stack, using, outputs);
                if (using == remainder) {
                    changes.add(inventory::setChanged);
                    return outputs;
                }
                remainder -= using;
            }
        }
        return null;
    }

    @Nullable
    static List<ItemStack> prepareCraft(BasinInput input, List<SizedIngredient> ingredients, Deque<Runnable> changes) {
        int ingredientSize = ingredients.size();
        if (ingredientSize == 0) {
            return new ArrayList<>();
        }
        if (ingredientSize == 1) {
            return prepareCraft(input, ingredients.getFirst(), changes);
        }
        List<ItemStack> usings = new ArrayList<>();
        List<IntObjectPair<ItemStack>> inputs = new LinkedList<>();
        int ingredientIndex = 0;
        Container inventory = input.items();
        Apply:
        for (int itemIndex = 0, inventorySize = inventory.getContainerSize(); ingredientIndex < ingredientSize; ingredientIndex++) {
            SizedIngredient ingredient = ingredients.get(ingredientIndex);
            int size = inputs.size();
            int remainder = ingredient.getCount();
            for (; itemIndex < inventorySize; itemIndex++) {
                ItemStack stack = inventory.getItem(itemIndex);
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
                        changes.add(() -> inventory.setItem(slot, ItemStack.EMPTY));
                        if (count == remainder) {
                            itemIndex++;
                            continue Apply;
                        }
                        remainder -= count;
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
                    }
                    usings.add(stack);
                    int slot = pair.leftInt();
                    changes.add(() -> inventory.setItem(slot, ItemStack.EMPTY));
                    if (count == remainder) {
                        ingredientIndex++;
                        break Apply;
                    }
                    remainder -= count;
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
                    }
                    usings.add(stack);
                    int slot = pair.leftInt();
                    changes.add(() -> inventory.setItem(slot, ItemStack.EMPTY));
                    if (count == remainder) {
                        continue Apply;
                    }
                    remainder -= count;
                }
            }
            return null;
        }
        changes.add(inventory::setChanged);
        List<ItemStack> outputs = new ArrayList<>();
        for (ItemStack stack : usings) {
            addRecipeRemainder(stack, stack.getCount(), outputs);
        }
        return outputs;
    }

    static boolean prepareFluidCraft(BasinInput input, @Nullable FluidIngredient ingredient, Deque<Runnable> changes) {
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
                    changes.add(() -> {
                        stack.setAmount(newAmount);
                        inventory.markDirty();
                    });
                    return true;
                } else {
                    int slot = fluidIndex;
                    if (remainder == amount) {
                        changes.add(() -> {
                            inventory.setStack(slot, FluidStack.EMPTY);
                            inventory.markDirty();
                        });
                        return true;
                    }
                    changes.add(() -> inventory.setStack(slot, FluidStack.EMPTY));
                    remainder -= amount;
                }
            }
        }
        return false;
    }

    static boolean prepareFluidCraft(BasinInput input, List<FluidIngredient> ingredients, Deque<Runnable> changes) {
        int ingredientSize = ingredients.size();
        if (ingredientSize == 0) {
            return true;
        }
        if (ingredientSize == 1) {
            return prepareFluidCraft(input, ingredients.getFirst(), changes);
        }
        List<IntObjectPair<FluidStack>> inputs = new LinkedList<>();
        int ingredientIndex = 0;
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
        changes.add(inventory::markDirty);
        return true;
    }

    int getIngredientSize();

    List<SizedIngredient> getIngredients();

    List<FluidIngredient> getFluidIngredients();

    boolean apply(BasinInput input);

    @Override
    default ItemStack assemble(BasinInput input) {
        return ItemStack.EMPTY;
    }
}
