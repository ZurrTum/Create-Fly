package com.zurrtum.create.content.logistics.filter;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.fluids.transfer.GenericItemEmptying;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.zurrtum.create.infrastructure.component.AttributeFilterWhitelistMode;
import com.zurrtum.create.infrastructure.component.ItemAttributeEntry;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class FilterItemStack {
    public static final Codec<FilterItemStack> CODEC = ItemStack.OPTIONAL_CODEC.xmap(FilterItemStack::of, FilterItemStack::item);
    private final ItemStack filterItemStack;
    private boolean fluidExtracted;
    private FluidStack filterFluidStack;

    public static FilterItemStack of(ItemStack filter) {
        if (!filter.getComponentChanges().isEmpty()) {
            if (filter.isOf(AllItems.FILTER)) {
                trimFilterComponents(filter);
                return new ListFilterItemStack(filter);
            }
            if (filter.isOf(AllItems.ATTRIBUTE_FILTER)) {
                trimFilterComponents(filter);
                return new AttributeFilterItemStack(filter);
            }
            if (filter.isOf(AllItems.PACKAGE_FILTER)) {
                trimFilterComponents(filter);
                return new PackageFilterItemStack(filter);
            }
        }

        return new FilterItemStack(filter);
    }

    public static FilterItemStack empty() {
        return of(ItemStack.EMPTY);
    }

    private static void trimFilterComponents(ItemStack filter) {
        filter.remove(DataComponentTypes.ENCHANTMENTS);
        filter.remove(DataComponentTypes.ATTRIBUTE_MODIFIERS);
    }

    public boolean isEmpty() {
        return filterItemStack.isEmpty();
    }

    public ItemStack item() {
        return filterItemStack;
    }

    public FluidStack fluid(World level) {
        resolveFluid(level);
        return filterFluidStack;
    }

    public boolean isFilterItem() {
        return filterItemStack.getItem() instanceof FilterItem;
    }

    //

    public boolean test(World world, ItemStack stack) {
        return test(world, stack, false);
    }

    public boolean test(World world, FluidStack stack) {
        return test(world, stack, true);
    }

    public boolean test(World world, ItemStack stack, boolean matchNBT) {
        if (isEmpty())
            return true;
        return FilterItem.testDirect(filterItemStack, stack, matchNBT);
    }

    public boolean test(World world, FluidStack stack, boolean matchNBT) {
        if (isEmpty())
            return true;
        if (stack.isEmpty())
            return false;

        resolveFluid(world);

        if (filterFluidStack.isEmpty())
            return false;
        if (!matchNBT)
            return filterFluidStack.getFluid().matchesType(stack.getFluid());
        return FluidStack.areFluidsAndComponentsEqual(filterFluidStack, stack);
    }

    //

    private void resolveFluid(World world) {
        if (!fluidExtracted) {
            fluidExtracted = true;
            if (GenericItemEmptying.canItemBeEmptied(world, filterItemStack))
                filterFluidStack = GenericItemEmptying.emptyItem(world, filterItemStack, true).getFirst();
        }
    }

    protected FilterItemStack(ItemStack filter) {
        filterItemStack = filter;
        filterFluidStack = FluidStack.EMPTY;
        fluidExtracted = false;
    }

    public static class ListFilterItemStack extends FilterItemStack {

        public List<FilterItemStack> containedItems;
        public boolean shouldRespectNBT;
        public boolean isBlacklist;

        protected ListFilterItemStack(ItemStack filter) {
            super(filter);
            boolean hasFilterItems = filter.contains(AllDataComponents.FILTER_ITEMS);

            containedItems = new ArrayList<>();
            for (ItemStack stack : FilterItem.getFilterItems(filter)) {
                if (!stack.isEmpty())
                    containedItems.add(FilterItemStack.of(stack));
            }

            shouldRespectNBT = hasFilterItems && filter.getOrDefault(AllDataComponents.FILTER_ITEMS_RESPECT_NBT, false);
            isBlacklist = hasFilterItems && filter.getOrDefault(AllDataComponents.FILTER_ITEMS_BLACKLIST, false);
        }

        @Override
        public boolean test(World world, ItemStack stack, boolean matchNBT) {
            if (containedItems.isEmpty())
                return super.test(world, stack, matchNBT);
            for (FilterItemStack filterItemStack : containedItems)
                if (filterItemStack.test(world, stack, shouldRespectNBT))
                    return !isBlacklist;
            return isBlacklist;
        }

        @Override
        public boolean test(World world, FluidStack stack, boolean matchNBT) {
            for (FilterItemStack filterItemStack : containedItems)
                if (filterItemStack.test(world, stack, shouldRespectNBT))
                    return !isBlacklist;
            return isBlacklist;
        }

    }

    public static class AttributeFilterItemStack extends FilterItemStack {
        public AttributeFilterWhitelistMode whitelistMode;
        public List<Pair<ItemAttribute, Boolean>> attributeTests;

        protected AttributeFilterItemStack(ItemStack filter) {
            super(filter);
            boolean defaults = !filter.contains(AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES);

            attributeTests = new ArrayList<>();
            whitelistMode = filter.getOrDefault(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE, AttributeFilterWhitelistMode.WHITELIST_DISJ);

            List<ItemAttributeEntry> attributes = defaults ? new ArrayList<>() : filter.get(AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES);
            //noinspection DataFlowIssue
            for (ItemAttributeEntry attributeEntry : attributes) {
                ItemAttribute attribute = attributeEntry.attribute();
                if (attribute != null)
                    attributeTests.add(Pair.of(attribute, attributeEntry.inverted()));
            }
        }

        @Override
        public boolean test(World world, FluidStack stack, boolean matchNBT) {
            return false;
        }

        @Override
        public boolean test(World world, ItemStack stack, boolean matchNBT) {
            if (attributeTests.isEmpty())
                return super.test(world, stack, matchNBT);
            for (Pair<ItemAttribute, Boolean> test : attributeTests) {
                ItemAttribute attribute = test.getFirst();
                boolean inverted = test.getSecond();
                boolean matches = attribute.appliesTo(stack, world) != inverted;

                if (matches) {
                    switch (whitelistMode) {
                        case BLACKLIST -> {
                            return false;
                        }
                        case WHITELIST_CONJ -> {
                            continue;
                        }
                        case WHITELIST_DISJ -> {
                            return true;
                        }
                    }
                } else {
                    switch (whitelistMode) {
                        case BLACKLIST, WHITELIST_DISJ -> {
                            continue;
                        }
                        case WHITELIST_CONJ -> {
                            return false;
                        }
                    }
                }
            }

            return switch (whitelistMode) {
                case BLACKLIST, WHITELIST_CONJ -> true;
                case WHITELIST_DISJ -> false;
            };

        }

    }

    public static class PackageFilterItemStack extends FilterItemStack {

        public String filterString;

        protected PackageFilterItemStack(ItemStack filter) {
            super(filter);
            filterString = PackageItem.getAddress(filter);
        }

        @Override
        public boolean test(World world, ItemStack stack, boolean matchNBT) {
            return (filterString.isBlank() && super.test(world, stack, matchNBT)) || PackageItem.isPackage(stack) && PackageItem.matchAddress(
                stack,
                filterString
            );
        }

        @Override
        public boolean test(World world, FluidStack stack, boolean matchNBT) {
            return false;
        }

    }
}
