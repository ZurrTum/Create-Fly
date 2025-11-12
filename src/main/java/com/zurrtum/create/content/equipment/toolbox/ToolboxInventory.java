package com.zurrtum.create.content.equipment.toolbox;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.foundation.item.ItemSlots;
import com.zurrtum.create.infrastructure.items.ItemInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ToolboxInventory implements ItemInventory {
    public static final int STACKS_PER_COMPARTMENT = 4;
    public static final int SIZE = 8 * STACKS_PER_COMPARTMENT;
    public static final Codec<ToolboxInventory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ItemSlots.maxSizeCodec(8 * STACKS_PER_COMPARTMENT).fieldOf("items").forGetter(ItemSlots::fromHandler),
        ItemStack.OPTIONAL_CODEC.listOf().fieldOf("filters").forGetter(toolbox -> toolbox.filters)
    ).apply(instance, ToolboxInventory::deserialize));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToolboxInventory> STREAM_CODEC = StreamCodec.composite(
        ItemSlots.STREAM_CODEC,
        ItemSlots::fromHandler,
        CatnipStreamCodecBuilders.list(ItemStack.OPTIONAL_STREAM_CODEC),
        toolbox -> toolbox.filters,
        ToolboxInventory::deserialize
    );

    public NonNullList<ItemStack> filters;
    NonNullList<ItemStack> stacks;
    @Nullable
    private final ToolboxBlockEntity blockEntity;
    private boolean limitedMode;

    public ToolboxInventory(@Nullable ToolboxBlockEntity be) {
        filters = NonNullList.withSize(8, ItemStack.EMPTY);
        stacks = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        blockEntity = be;
        limitedMode = false;
    }

    public void inLimitedMode(Consumer<ToolboxInventory> action) {
        limitedMode = true;
        action.accept(this);
        limitedMode = false;
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot >= SIZE) {
            return ItemStack.EMPTY;
        }
        return stacks.get(slot);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (!stack.getItem().canFitInsideContainerItems()) {
            return false;
        }
        if (slot >= SIZE) {
            return false;
        }
        ItemStack filter = filters.get(slot / STACKS_PER_COMPARTMENT);
        boolean empty = filter.isEmpty();
        if (limitedMode && empty) {
            return false;
        }
        return empty || canItemsShareCompartment(filter, stack);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= SIZE) {
            return;
        }
        stacks.set(slot, stack);
        if (!stack.isEmpty()) {
            int compartment = slot / STACKS_PER_COMPARTMENT;
            if (filters.get(compartment).isEmpty()) {
                filters.set(compartment, stack.copyWithCount(1));
            }
        }
    }

    public int distributeToCompartment(@NotNull ItemStack stack, int compartment, boolean simulate) {
        if (stack.isEmpty() || !stack.getItem().canFitInsideContainerItems()) {
            return 0;
        }
        ItemStack filter = filters.get(compartment);
        if (filter.isEmpty() || !canItemsShareCompartment(filter, stack)) {
            return 0;
        }
        int maxAmount = stack.getCount();
        int stackSize = stack.getMaxStackSize();
        if (simulate) {
            int count = 0;
            for (int i = compartment * STACKS_PER_COMPARTMENT, end = i + STACKS_PER_COMPARTMENT; i < end; i++) {
                ItemStack target = getItem(i);
                if (target.isEmpty()) {
                    return maxAmount;
                } else {
                    count += stackSize - target.getCount();
                    if (count >= maxAmount) {
                        return maxAmount;
                    }
                }
            }
            return count;
        } else {
            int remaining = maxAmount;
            for (int i = compartment * STACKS_PER_COMPARTMENT, end = i + STACKS_PER_COMPARTMENT; i < end; i++) {
                ItemStack target = getItem(i);
                if (target.isEmpty()) {
                    setItem(i, directCopy(stack, remaining));
                    setChanged();
                    return maxAmount;
                } else {
                    int count = target.getCount();
                    if (count != stackSize) {
                        int insert = Math.min(remaining, stackSize - count);
                        target.setCount(count + insert);
                        if (remaining == insert) {
                            setChanged();
                            return maxAmount;
                        }
                        remaining -= insert;
                    }
                }
            }
            if (remaining == maxAmount) {
                return 0;
            }
            setChanged();
            return maxAmount - remaining;
        }
    }

    public ItemStack takeFromCompartment(int maxAmount, int compartment, boolean simulate) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        int index = compartment * STACKS_PER_COMPARTMENT;
        if (simulate) {
            for (int i = index + STACKS_PER_COMPARTMENT - 1; i >= index; i--) {
                ItemStack findStack = stacks.get(i);
                if (findStack.isEmpty()) {
                    continue;
                }
                int count = findStack.getCount();
                if (count >= maxAmount) {
                    return directCopy(findStack, maxAmount);
                }
                for (int j = i - 1; j >= index; j--) {
                    ItemStack stack = getItem(i);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    count += stack.getCount();
                    if (count < maxAmount) {
                        continue;
                    }
                    return directCopy(findStack, maxAmount);
                }
                return directCopy(findStack, count);
            }
            return ItemStack.EMPTY;
        } else {
            ItemStack stack = takeFromCompartment(maxAmount, index, index + STACKS_PER_COMPARTMENT - 1);
            if (stack == ItemStack.EMPTY) {
                return stack;
            }
            setChanged();
            return stack;
        }
    }

    protected ItemStack takeFromCompartment(int maxAmount, int start, int end) {
        for (int i = end; i >= start; i--) {
            ItemStack findStack = stacks.get(i);
            if (findStack.isEmpty()) {
                continue;
            }
            int count = findStack.getCount();
            if (count > maxAmount) {
                findStack.setCount(count - maxAmount);
                return directCopy(findStack, maxAmount);
            }
            setItem(i, ItemStack.EMPTY);
            if (count == maxAmount) {
                return findStack;
            }
            int remaining = maxAmount - count;
            for (int j = i - 1; j >= start; j--) {
                ItemStack stack = stacks.get(j);
                if (stack.isEmpty()) {
                    continue;
                }
                count = stack.getCount();
                if (count < remaining) {
                    setItem(i, ItemStack.EMPTY);
                    remaining -= count;
                    continue;
                }
                if (count == remaining) {
                    setItem(i, ItemStack.EMPTY);
                } else {
                    stack.setCount(count - remaining);
                }
                findStack.setCount(maxAmount);
                return findStack;
            }
            findStack.setCount(maxAmount - remaining);
            return findStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setChanged() {
        if (blockEntity != null) {
            blockEntity.notifyUpdate();
        }
    }

    public static ItemStack cleanItemNBT(ItemStack stack) {
        if (stack.is(AllItems.BELT_CONNECTOR))
            stack.remove(AllDataComponents.BELT_FIRST_SHAFT);
        return stack;
    }

    public static boolean canItemsShareCompartment(ItemStack stack1, ItemStack stack2) {
        if (!stack1.isStackable() && !stack2.isStackable() && stack1.isDamageableItem() && stack2.isDamageableItem())
            return stack1.getItem() == stack2.getItem();
        if (stack1.is(AllItems.BELT_CONNECTOR) && stack2.is(AllItems.BELT_CONNECTOR))
            return true;
        return ItemStack.isSameItemSameComponents(stack1, stack2);
    }

    public void write(ValueOutput view) {
        view.store("Items", ItemSlots.CODEC, ItemSlots.fromHandler(this));
        view.store("Compartments", CreateCodecs.ITEM_LIST_CODEC, filters);
    }

    public void read(ValueInput view) {
        view.read("Items", ItemSlots.CODEC).ifPresentOrElse(
            slots -> {
                boolean[] fill = new boolean[SIZE];
                slots.forEach((slot, stack) -> {
                    stacks.set(slot, stack);
                    fill[slot] = true;
                });
                for (int i = 0; i < SIZE; i++) {
                    if (!fill[i]) {
                        stacks.set(i, ItemStack.EMPTY);
                    }
                }
            }, stacks::clear
        );
        view.read("Compartments", CreateCodecs.ITEM_LIST_CODEC).ifPresentOrElse(
            list -> {
                for (int i = 0, size = Math.min(list.size(), SIZE); i < size; i++) {
                    filters.set(i, list.get(i));
                }
            }, filters::clear
        );
    }

    private static ToolboxInventory deserialize(ItemSlots slots, List<ItemStack> filters) {
        ToolboxInventory inventory = new ToolboxInventory(null);
        slots.forEach(inventory.stacks::set);
        for (int i = 0, size = Math.min(filters.size(), SIZE); i < size; i++) {
            inventory.filters.set(i, filters.get(i));
        }
        return inventory;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof ToolboxInventory that))
            return false;

        return limitedMode == that.limitedMode && filters.equals(that.filters) && Objects.equals(blockEntity, that.blockEntity);
    }

    @Override
    public int hashCode() {
        int result = filters.hashCode();
        result = 31 * result + Objects.hashCode(blockEntity);
        result = 31 * result + Boolean.hashCode(limitedMode);
        return result;
    }
}
