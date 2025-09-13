package com.zurrtum.create.foundation.item;

import com.zurrtum.create.AllTransfer;
import com.zurrtum.create.content.logistics.box.PackageEntity;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ItemHelper {
    private static final Map<BlockPos, InventoryCache> INV_CACHE = new Object2ReferenceOpenHashMap<>();

    public static boolean sameItem(ItemStack stack, ItemStack otherStack) {
        return !otherStack.isEmpty() && stack.isOf(otherStack.getItem());
    }

    public static Predicate<ItemStack> sameItemPredicate(ItemStack stack) {
        return s -> sameItem(stack, s);
    }

    public static List<ItemStack> multipliedOutput(ItemStack out, int count) {
        if (out.isEmpty()) {
            return new ArrayList<>(0);
        }
        int total = count * out.getCount();
        int max = out.getMaxCount();
        int size = total / max;
        int remaining = total % max;
        boolean hasRemaining = remaining != 0;
        List<ItemStack> stacks = new ArrayList<>(hasRemaining ? size + 1 : size);
        stacks.add(out);
        if (size != 0) {
            out.setCount(max);
            for (int i = 1; i < size; i++) {
                stacks.add(out.copy());
            }
            if (hasRemaining) {
                stacks.add(out.copyWithCount(remaining));
            }
        } else {
            out.setCount(total);
        }
        return stacks;
    }

    public static List<ItemStack> multipliedOutput(List<ItemStack> out, int count) {
        if (out.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack stack : out) {
            int total = count * stack.getCount();
            int max = stack.getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 64);
            int size = total / max;
            stacks.add(stack);
            if (size != 0) {
                stack.setCount(max);
                for (int i = 1; i < size; i++) {
                    stacks.add(stack.copy());
                }
                int remaining = total % max;
                if (remaining != 0) {
                    stacks.add(stack.copyWithCount(remaining));
                }
            } else {
                stack.setCount(total);
            }
        }
        return stacks;
    }

    public static void addToList(ItemStack stack, List<ItemStack> stacks) {
        for (ItemStack s : stacks) {
            if (!ItemStack.areItemsAndComponentsEqual(stack, s))
                continue;
            int transferred = Math.min(s.getMaxCount() - s.getCount(), stack.getCount());
            s.increment(transferred);
            stack.decrement(transferred);
        }
        if (stack.getCount() > 0)
            stacks.add(stack);
    }

    public static <T extends IBE<? extends BlockEntity>> int calcRedstoneFromBlockEntity(T ibe, World level, BlockPos pos) {
        return ibe.getBlockEntityOptional(level, pos).map(be -> getInventory(level, pos, null)).map(ItemHelper::calcRedstoneFromInventory).orElse(0);
    }

    public static int calcRedstoneFromInventory(@Nullable Inventory inv) {
        if (inv == null)
            return 0;
        int i = 0;
        float f = 0.0F;
        int totalSlots = inv.size();

        for (int j = 0, size = totalSlots; j < size; ++j) {
            int slotLimit = inv.getMaxCountPerStack();
            if (slotLimit == 0) {
                totalSlots--;
                continue;
            }
            ItemStack itemstack = inv.getStack(j);
            if (!itemstack.isEmpty()) {
                f += (float) itemstack.getCount() / (float) Math.min(slotLimit, itemstack.getMaxCount());
                ++i;
            }
        }

        if (totalSlots == 0)
            return 0;

        f = f / totalSlots;
        return MathHelper.floor(f * 14.0F) + (i > 0 ? 1 : 0);
    }

    //TODO
    //    public static List<Pair<Ingredient, MutableInt>> condenseIngredients(DefaultedList<Ingredient> recipeIngredients) {
    //        List<Pair<Ingredient, MutableInt>> actualIngredients = new ArrayList<>();
    //        Ingredients:
    //        for (Ingredient igd : recipeIngredients) {
    //            for (Pair<Ingredient, MutableInt> pair : actualIngredients) {
    //                ItemStack[] stacks1 = pair.getFirst().getItems();
    //                ItemStack[] stacks2 = igd.getItems();
    //                if (stacks1.length != stacks2.length)
    //                    continue;
    //                for (int i = 0; i <= stacks1.length; i++) {
    //                    if (i == stacks1.length) {
    //                        pair.getSecond().increment();
    //                        continue Ingredients;
    //                    }
    //                    if (!ItemStack.areEqual(stacks1[i], stacks2[i]))
    //                        break;
    //                }
    //            }
    //            actualIngredients.add(Pair.of(igd, new MutableInt(1)));
    //        }
    //        return actualIngredients;
    //    }

    public static boolean matchIngredients(Ingredient i1, Ingredient i2) {
        if (i1 == i2)
            return true;
        RegistryEntryList<Item> entries1 = i1.entries;
        RegistryEntryList<Item> entries2 = i2.entries;
        int size = entries1.size();
        if (size == entries2.size()) {
            for (int i = 0; i < size; i++)
                if (!entries1.contains(entries2.get(i)))
                    return false;
            return true;
        }
        return false;
    }

    public static boolean matchAllIngredients(List<Ingredient> ingredients) {
        if (ingredients.size() <= 1)
            return true;
        Ingredient firstIngredient = ingredients.getFirst();
        for (int i = 1; i < ingredients.size(); i++)
            if (!matchIngredients(firstIngredient, ingredients.get(i)))
                return false;
        return true;
    }

    public static enum ExtractionCountMode {
        EXACTLY,
        UPTO
    }

    public static ItemStack extractItem(Inventory inventory, int slot, int amount, boolean simulate) {
        ItemStack stack = inventory.getStack(slot);
        if (stack.isEmpty() || (inventory instanceof SidedInventory sidedInventory && !sidedInventory.canExtract(slot, stack, null))) {
            return ItemStack.EMPTY;
        }
        int extract = Math.min(amount, stack.getCount());
        if (simulate) {
            return stack.copyWithCount(extract);
        } else if (extract == amount) {
            inventory.setStack(slot, ItemStack.EMPTY);
            inventory.markDirty();
            return stack;
        } else {
            ItemStack result = stack.copy();
            result.setCount(extract);
            stack.decrement(extract);
            inventory.markDirty();
            return result;
        }
    }
    //TODO
    //    public static ItemStack extract(IItemHandler inv, Predicate<ItemStack> test, boolean simulate) {
    //        return extract(inv, test, ExtractionCountMode.UPTO, 64, simulate);
    //    }
    //
    //    public static ItemStack extract(IItemHandler inv, Predicate<ItemStack> test, int exactAmount, boolean simulate) {
    //        return extract(inv, test, ExtractionCountMode.EXACTLY, exactAmount, simulate);
    //    }
    //
    //    public static ItemStack extract(IItemHandler inv, Predicate<ItemStack> test, ExtractionCountMode mode, int amount, boolean simulate) {
    //        ItemStack extracting = ItemStack.EMPTY;
    //        boolean amountRequired = mode == ExtractionCountMode.EXACTLY;
    //        boolean checkHasEnoughItems = amountRequired;
    //        boolean hasEnoughItems = !checkHasEnoughItems;
    //        boolean potentialOtherMatch = false;
    //        int maxExtractionCount = amount;
    //
    //        Extraction:
    //        do {
    //            extracting = ItemStack.EMPTY;
    //
    //            for (int slot = 0; slot < inv.getSlots(); slot++) {
    //                int amountToExtractFromThisSlot = Math.min(
    //                    maxExtractionCount - extracting.getCount(),
    //                    inv.getStackInSlot(slot).getOrDefault(DataComponents.MAX_STACK_SIZE, 64)
    //                );
    //                ItemStack stack = inv.extractItem(slot, amountToExtractFromThisSlot, true);
    //
    //                if (stack.isEmpty())
    //                    continue;
    //                if (!test.test(stack))
    //                    continue;
    //                if (!extracting.isEmpty() && !canItemStackAmountsStack(stack, extracting)) {
    //                    potentialOtherMatch = true;
    //                    continue;
    //                }
    //
    //                if (extracting.isEmpty())
    //                    extracting = stack.copy();
    //                else
    //                    extracting.grow(stack.getCount());
    //
    //                if (!simulate && hasEnoughItems)
    //                    inv.extractItem(slot, stack.getCount(), false);
    //
    //                if (extracting.getCount() >= maxExtractionCount) {
    //                    if (checkHasEnoughItems) {
    //                        hasEnoughItems = true;
    //                        checkHasEnoughItems = false;
    //                        continue Extraction;
    //                    } else {
    //                        break Extraction;
    //                    }
    //                }
    //            }
    //
    //            if (!extracting.isEmpty() && !hasEnoughItems && potentialOtherMatch) {
    //                ItemStack blackListed = extracting.copy();
    //                test = test.and(i -> !ItemStack.isSameItemSameComponents(i, blackListed));
    //                continue;
    //            }
    //
    //            if (checkHasEnoughItems)
    //                checkHasEnoughItems = false;
    //            else
    //                break Extraction;
    //
    //        } while (true);
    //
    //        if (amountRequired && extracting.getCount() < amount)
    //            return ItemStack.EMPTY;
    //
    //        return extracting;
    //    }
    //
    //    public static ItemStack extract(IItemHandler inv, Predicate<ItemStack> test, Function<ItemStack, Integer> amountFunction, boolean simulate) {
    //        ItemStack extracting = ItemStack.EMPTY;
    //        int maxExtractionCount = 64;
    //
    //        for (int slot = 0; slot < inv.getSlots(); slot++) {
    //            if (extracting.isEmpty()) {
    //                ItemStack stackInSlot = inv.getStackInSlot(slot);
    //                if (stackInSlot.isEmpty() || !test.test(stackInSlot))
    //                    continue;
    //                int maxExtractionCountForItem = amountFunction.apply(stackInSlot);
    //                if (maxExtractionCountForItem == 0)
    //                    continue;
    //                maxExtractionCount = Math.min(maxExtractionCount, maxExtractionCountForItem);
    //            }
    //
    //            ItemStack stack = inv.extractItem(slot, maxExtractionCount - extracting.getCount(), true);
    //
    //            if (!test.test(stack))
    //                continue;
    //            if (!extracting.isEmpty() && !canItemStackAmountsStack(stack, extracting))
    //                continue;
    //
    //            if (extracting.isEmpty())
    //                extracting = stack.copy();
    //            else
    //                extracting.grow(stack.getCount());
    //
    //            if (!simulate)
    //                inv.extractItem(slot, stack.getCount(), false);
    //            if (extracting.getCount() >= maxExtractionCount)
    //                break;
    //        }
    //
    //        return extracting;
    //    }

    public static boolean canItemStackAmountsStack(ItemStack a, ItemStack b) {
        return ItemStack.areItemsAndComponentsEqual(a, b) && a.getCount() + b.getCount() <= a.getMaxCount();
    }

    public static ItemStack fromItemEntity(Entity entityIn) {
        if (!entityIn.isAlive())
            return ItemStack.EMPTY;
        if (entityIn instanceof PackageEntity packageEntity) {
            return packageEntity.getBox();
        }
        return entityIn instanceof ItemEntity itemEntity ? itemEntity.getStack() : ItemStack.EMPTY;
    }

    public static void fillItemStackHandler(ContainerComponent contents, ItemStackHandler inv) {
        List<ItemStack> itemStacks = contents.stream().toList();

        for (int i = 0; i < itemStacks.size(); i++) {
            inv.setStack(i, itemStacks.get(i));
        }
    }

    public static ContainerComponent containerContentsFromHandler(ItemStackHandler handler) {
        return ContainerComponent.fromStacks(handler.getStacks());
    }

    public static ItemStack limitCountToMaxStackSize(ItemStack stack, boolean simulate) {
        int count = stack.getCount();
        int max = stack.getMaxCount();
        if (count <= max)
            return ItemStack.EMPTY;
        ItemStack remainder = stack.copyWithCount(count - max);
        if (!simulate)
            stack.setCount(max);
        return remainder;
    }

    public static void copyContents(Inventory from, Inventory to) {
        if (from.size() != to.size()) {
            throw new IllegalArgumentException("Slot count mismatch");
        }

        for (int slot = to.size() - 1; slot >= 0; slot--) {
            to.setStack(slot, ItemStack.EMPTY);
        }

        for (int i = 0; i < from.size(); i++) {
            to.setStack(i, from.getStack(i).copy());
        }
    }

    public static List<ItemStack> getNonEmptyStacks(ItemStackHandler handler) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0, size = handler.size(); i < size; i++) {
            ItemStack stack = handler.getStack(i);
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    public static Inventory getInventory(World world, BlockPos pos, Direction direction) {
        return getInventory(world, pos, null, null, direction);
    }

    public static Inventory getInventory(World world, BlockPos pos, BlockState state, BlockEntity blockEntity, Direction direction) {
        if (state == null) {
            state = blockEntity != null ? blockEntity.getCachedState() : world.getBlockState(pos);
        }
        Block block = state.getBlock();
        if (block instanceof ItemInventoryProvider<?> provider) {
            return provider.getInventory(state, world, pos, blockEntity, direction);
        }
        if (block instanceof InventoryProvider provider) {
            return provider.getInventory(state, world, pos);
        }
        if (blockEntity == null && state.hasBlockEntity()) {
            blockEntity = world.getBlockEntity(pos);
        }
        if (blockEntity instanceof Inventory inventory) {
            if (inventory instanceof ChestBlockEntity && block instanceof ChestBlock chestBlock) {
                inventory = ChestBlock.getInventory(chestBlock, state, world, pos, true);
            }
            return inventory;
        }
        return AllTransfer.getInventory(world, pos, state, blockEntity, direction);
    }

    public static Supplier<Inventory> getInventoryCache(
        ServerWorld world,
        BlockPos pos,
        Direction direction,
        BiPredicate<BlockEntity, Direction> filter
    ) {
        InventoryCache cache = new InventoryCache(world, pos, direction, filter);
        INV_CACHE.put(pos, cache);
        return cache;
    }

    public static void invalidateInventoryCache(BlockPos pos) {
        InventoryCache cache = ItemHelper.INV_CACHE.get(pos);
        if (cache != null) {
            cache.invalidate();
        }
    }
}