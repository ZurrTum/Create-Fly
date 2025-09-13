package com.zurrtum.create.infrastructure.items;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.collection.DefaultedList;
import org.apache.commons.lang3.mutable.MutableInt;

public class ItemStackHandler implements ItemInventory {
    public static final Codec<ItemStackHandler> CODEC = Codec.of(ItemStackHandler::encode, ItemStackHandler::decode);

    protected final DefaultedList<ItemStack> stacks;

    public ItemStackHandler() {
        this(1);
    }

    public ItemStackHandler(int size) {
        this.stacks = DefaultedList.ofSize(size, ItemStack.EMPTY);
    }

    @Override
    public int size() {
        return stacks.size();
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot >= size()) {
            return ItemStack.EMPTY;
        }
        return stacks.get(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot >= size()) {
            return;
        }
        stacks.set(slot, stack);
    }

    public DefaultedList<ItemStack> getStacks() {
        return stacks;
    }

    public void writeSlots(WriteView view) {
        view.put("Inventory", CreateCodecs.ITEM_LIST_CODEC, stacks);
    }

    public void readSlots(ReadView view) {
        view.read("Inventory", CreateCodecs.ITEM_LIST_CODEC).ifPresentOrElse(
            list -> {
                for (int i = 0, size = list.size(); i < size; i++) {
                    stacks.set(i, list.get(i));
                }
            }, stacks::clear
        );
    }

    public void write(WriteView view) {
        WriteView.ListAppender<ItemStack> list = view.getListAppender("Inventory", ItemStack.CODEC);
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            list.add(stack);
        }
    }

    public void read(ReadView view) {
        ReadView.TypedListReadView<ItemStack> list = view.getTypedListView("Inventory", ItemStack.CODEC);
        int i = 0;
        for (ItemStack itemStack : list) {
            stacks.set(i++, itemStack);
        }
        for (int size = stacks.size(); i < size; i++) {
            stacks.set(i, ItemStack.EMPTY);
        }
    }

    private static <T> DataResult<T> encode(ItemStackHandler input, DynamicOps<T> ops, T prefix) {
        RecordBuilder<T> map = ops.mapBuilder();
        map.add("Size", ops.createInt(input.stacks.size()));
        ListBuilder<T> list = ops.listBuilder();
        for (ItemStack stack : input.stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            list.add(ItemStack.CODEC.encodeStart(ops, stack));
        }
        map.add("Stacks", list.build(ops.empty()));
        return map.build(prefix);
    }

    private static <T> DataResult<Pair<ItemStackHandler, T>> decode(DynamicOps<T> ops, T input) {
        MapLike<T> map = ops.getMap(input).getOrThrow();
        int size = ops.getNumberValue(map.get("Size")).getOrThrow().intValue();
        ItemStackHandler handler = new ItemStackHandler(size);
        MutableInt i = new MutableInt();
        ops.getList(map.get("Stacks")).getOrThrow().accept(item -> {
            handler.stacks.set(i.getAndIncrement(), ItemStack.CODEC.parse(ops, item).result().orElse(ItemStack.EMPTY));
        });
        return DataResult.success(Pair.of(handler, ops.empty()));
    }
}
