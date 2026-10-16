package com.zurrtum.create.infrastructure.items;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SuppressWarnings("unused")
public interface ContainerExtension extends Iterable<ItemStack> {
    Hash.Strategy<ItemStack> ITEM_STACK_HASH_STRATEGY = new Hash.Strategy<>() {
        @Override
        public boolean equals(@Nullable ItemStack stack, @Nullable ItemStack other) {
            return stack == other || stack != null && other != null && ItemStack.isSameItemSameComponents(stack, other);
        }

        @Override
        public int hashCode(ItemStack stack) {
            return ItemStack.hashItemAndComponents(stack);
        }
    };

    default int count(ItemStack stack, @Nullable Direction side) {
        return count(stack);
    }

    default int count(ItemStack stack) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return 0;
        }
        return count(stack, maxAmount);
    }

    default int count(ItemStack stack, int maxAmount, @Nullable Direction side) {
        return count(stack, maxAmount);
    }

    default int count(ItemStack stack, int maxAmount) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default ItemStack count(Predicate<ItemStack> predicate, @Nullable Direction side) {
        return count(predicate);
    }

    default ItemStack count(Predicate<ItemStack> predicate) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default ItemStack count(Predicate<ItemStack> predicate, int maxAmount, @Nullable Direction side) {
        return count(predicate, maxAmount);
    }

    default ItemStack count(Predicate<ItemStack> predicate, int maxAmount) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default ItemStack preciseCount(Predicate<ItemStack> predicate, int maxAmount, @Nullable Direction side) {
        return preciseCount(predicate, maxAmount);
    }

    default ItemStack preciseCount(Predicate<ItemStack> predicate, int maxAmount) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default int countAll(Predicate<ItemStack> predicate, int maxAmount, @Nullable Direction side) {
        return countAll(predicate, maxAmount);
    }

    default int countAll(Predicate<ItemStack> predicate, int maxAmount) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default ItemStack countAny(@Nullable Direction side) {
        return countAny();
    }

    default ItemStack countAny() {
        throw new RuntimeException("Implemented via Mixin");
    }

    default ItemStack countAny(int maxAmount, @Nullable Direction side) {
        return extractAny(maxAmount);
    }

    default ItemStack countAny(int maxAmount) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default int countSpace(ItemStack stack, @Nullable Direction side) {
        return countSpace(stack);
    }

    default int countSpace(ItemStack stack) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return 0;
        }
        return countSpace(stack, maxAmount);
    }

    default int countSpace(ItemStack stack, int maxAmount, @Nullable Direction side) {
        return countSpace(stack, maxAmount);
    }

    default int countSpace(ItemStack stack, int maxAmount) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default int countSpace(ItemStack stack, int maxAmount, int start, int end, @Nullable Direction side) {
        return countSpace(stack, maxAmount, start, end);
    }

    default int countSpace(ItemStack stack, int maxAmount, int start, int end) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default boolean countSpace(List<ItemStack> stacks, @Nullable Direction side) {
        return countSpace(stacks);
    }

    default boolean countSpace(List<ItemStack> stacks) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default boolean countSpace(List<ItemStack> stacks, int start, int end, @Nullable Direction side) {
        return countSpace(stacks, start, end);
    }

    default boolean countSpace(List<ItemStack> stacks, int start, int end) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @SuppressWarnings("deprecation")
    default ItemStack directCopy(ItemStack stack, int count) {
        assert stack.item != null;
        ItemStack copy = new ItemStack(stack.item, count, stack.components.copy());
        copy.setPopTime(stack.getPopTime());
        return copy;
    }

    default int extract(ItemStack stack, @Nullable Direction side) {
        return extract(stack);
    }

    default int extract(ItemStack stack) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return 0;
        }
        return extract(stack, maxAmount);
    }

    default int extract(ItemStack stack, int maxAmount, @Nullable Direction side) {
        return extract(stack, maxAmount);
    }

    default int extract(ItemStack stack, int maxAmount) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default ItemStack extract(Predicate<ItemStack> predicate, @Nullable Direction side) {
        return extract(predicate);
    }

    default ItemStack extract(Predicate<ItemStack> predicate) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default ItemStack extract(Predicate<ItemStack> predicate, int maxAmount, @Nullable Direction side) {
        return extract(predicate, maxAmount);
    }

    default ItemStack extract(Predicate<ItemStack> predicate, int maxAmount) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default List<ItemStack> extract(List<ItemStack> stacks, @Nullable Direction side) {
        return extract(stacks);
    }

    default List<ItemStack> extract(List<ItemStack> stacks) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default int extractAll(Predicate<ItemStack> predicate, int maxAmount, @Nullable Direction side) {
        return extractAll(predicate, maxAmount);
    }

    default int extractAll(Predicate<ItemStack> predicate, int maxAmount) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default ItemStack extractAny(@Nullable Direction side) {
        return extractAny();
    }

    default ItemStack extractAny() {
        throw new RuntimeException("Implemented via Mixin");
    }

    default ItemStack extractAny(int maxAmount, @Nullable Direction side) {
        return extractAny(maxAmount);
    }

    default ItemStack extractAny(int maxAmount) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default ItemStack extractAnyMax(@Nullable Direction side) {
        return extractAnyMax();
    }

    default ItemStack extractAnyMax() {
        throw new RuntimeException("Implemented via Mixin");
    }

    default ItemStack extractMax(Predicate<ItemStack> predicate, @Nullable Direction side) {
        return extractMax(predicate);
    }

    default ItemStack extractMax(Predicate<ItemStack> predicate) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default int forceInsert(ItemStack stack) {
        return insert(stack);
    }

    default int forceInsert(ItemStack stack, int maxAmount) {
        return insert(stack, maxAmount);
    }

    default boolean forcePreciseInsert(ItemStack stack) {
        return preciseInsert(stack);
    }

    default boolean forcePreciseInsert(ItemStack stack, int maxAmount) {
        return preciseInsert(stack, maxAmount);
    }

    default int insert(ItemStack stack, @Nullable Direction side) {
        return insert(stack);
    }

    default int insert(ItemStack stack) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return 0;
        }
        return insert(stack, maxAmount);
    }

    default int insert(ItemStack stack, int maxAmount, @Nullable Direction side) {
        return insert(stack, maxAmount);
    }

    default int insert(ItemStack stack, int maxAmount) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default int insert(ItemStack stack, int maxAmount, int start, int end, @Nullable Direction side) {
        return insert(stack, maxAmount, start, end);
    }

    default int insert(ItemStack stack, int maxAmount, int start, int end) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default List<ItemStack> insert(List<ItemStack> stacks, @Nullable Direction side) {
        return insert(stacks);
    }

    default List<ItemStack> insert(List<ItemStack> stacks) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default List<ItemStack> insert(List<ItemStack> stacks, int start, int end, @Nullable Direction side) {
        return insert(stacks, start, end);
    }

    default List<ItemStack> insert(List<ItemStack> stacks, int start, int end) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default int insertExist(ItemStack stack, @Nullable Direction side) {
        return insertExist(stack);
    }

    default int insertExist(ItemStack stack, int maxAmount, @Nullable Direction side) {
        return insertExist(stack, maxAmount);
    }

    default int insertExist(ItemStack stack) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return 0;
        }
        return insertExist(stack, maxAmount);
    }

    default int insertExist(ItemStack stack, int maxAmount) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default Iterator<ItemStack> iterator(@Nullable Direction side) {
        return iterator();
    }

    default boolean matches(ItemStack stack, ItemStack otherStack) {
        if (stack.is(otherStack.getItem())) {
            PatchedDataComponentMap stackComponents = stack.components;
            PatchedDataComponentMap otherStackComponents = otherStack.components;
            if (stackComponents == otherStackComponents) {
                return true;
            }
            Reference2ObjectMap<DataComponentType<?>, Object> stackComponentMap = stackComponents.patch;
            Reference2ObjectMap<DataComponentType<?>, Object> otherStackComponentMap = otherStackComponents.patch;
            if (stackComponentMap == otherStackComponentMap) {
                return true;
            }
            int stackComponentCount = stackComponentMap.size();
            if (stackComponentMap.containsKey(DataComponents.MAX_STACK_SIZE)) {
                stackComponentCount--;
            }
            int otherStackComponentCount = otherStackComponentMap.size();
            boolean hasMaxCapacityComponent = false;
            if (otherStackComponentMap.containsKey(DataComponents.MAX_STACK_SIZE)) {
                otherStackComponentCount--;
                hasMaxCapacityComponent = true;
            }
            if (stackComponentCount != otherStackComponentCount) {
                return false;
            }
            if (hasMaxCapacityComponent) {
                ObjectSet<Reference2ObjectMap.Entry<DataComponentType<?>, Object>> stackComponentSet = stackComponentMap.reference2ObjectEntrySet();
                for (Reference2ObjectMap.Entry<DataComponentType<?>, Object> componentEntry : otherStackComponentMap.reference2ObjectEntrySet()) {
                    if (!stackComponentSet.contains(componentEntry) && componentEntry.getKey() != DataComponents.MAX_STACK_SIZE) {
                        return false;
                    }
                }
                return true;
            }
            return stackComponentMap.reference2ObjectEntrySet()
                .containsAll(otherStackComponentMap.reference2ObjectEntrySet());
        }
        return false;
    }

    default ItemStack onExtract(ItemStack stack) {
        return stack;
    }

    default boolean preciseExtract(ItemStack stack, @Nullable Direction side) {
        return preciseExtract(stack);
    }

    default boolean preciseExtract(ItemStack stack) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default ItemStack preciseExtract(Predicate<ItemStack> predicate, int maxAmount, @Nullable Direction side) {
        return preciseExtract(predicate, maxAmount);
    }

    default ItemStack preciseExtract(Predicate<ItemStack> predicate, int maxAmount) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default boolean preciseInsert(ItemStack stack, @Nullable Direction side) {
        return preciseInsert(stack);
    }

    default boolean preciseInsert(ItemStack stack) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return true;
        }
        return preciseInsert(stack, maxAmount);
    }

    default boolean preciseInsert(ItemStack stack, int maxAmount, @Nullable Direction side) {
        return preciseInsert(stack, maxAmount);
    }

    default boolean preciseInsert(ItemStack stack, int maxAmount) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default boolean preciseInsert(List<ItemStack> stacks, @Nullable Direction side) {
        return preciseInsert(stacks);
    }

    default boolean preciseInsert(List<ItemStack> stacks) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default boolean update(
        Predicate<ItemStack> predicate,
        Function<ItemStack, ItemStack> update,
        @Nullable Direction side
    ) {
        return update(predicate, update);
    }

    default boolean update(Predicate<ItemStack> predicate, Function<ItemStack, ItemStack> update) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default ItemStack removeMaxSize(ItemStack stack, Integer max) {
        PatchedDataComponentMap components = stack.components;
        components.ensureMapOwnership();
        components.patch.remove(DataComponents.MAX_STACK_SIZE, max);
        return stack;
    }

    default void setMaxSize(ItemStack stack, Integer max) {
        PatchedDataComponentMap components = stack.components;
        components.ensureMapOwnership();
        components.patch.put(DataComponents.MAX_STACK_SIZE, max);
    }

    default Stream<ItemStack> stream(@Nullable Direction side) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(side), Spliterator.ORDERED), false);
    }

    default Stream<ItemStack> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false);
    }
}
