package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.RenderType;

public class ItemChunkLayerSortedListBuilder<T> {
    private static final ThreadLocal<ItemChunkLayerSortedListBuilder<?>> THREAD_LOCAL = ThreadLocal.withInitial(ItemChunkLayerSortedListBuilder::new);

    private final List<ObjectArrayList<T>> lists;

    private ItemChunkLayerSortedListBuilder() {
        int size = BakedItemModelBufferer.getChunkLayers().size();
        lists = new ArrayList<>(size);
        for (int layerIndex = 0; layerIndex < size; layerIndex++) {
            lists.add(new ObjectArrayList<>());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> ItemChunkLayerSortedListBuilder<T> getThreadLocal() {
        return (ItemChunkLayerSortedListBuilder<T>) THREAD_LOCAL.get();
    }

    public void add(RenderType renderType, T obj) {
        Integer layerIndex = BakedItemModelBufferer.getChunkLayers().get(renderType);
        if (layerIndex == null) {
            throw new IllegalArgumentException("RenderType '" + renderType + "' is not a chunk layer");
        }
        int size = lists.size();
        if (size > layerIndex) {
            lists.get(layerIndex).add(obj);
            return;
        }
        for (; size < layerIndex; size++) {
            lists.add(new ObjectArrayList<>());
        }
        ObjectArrayList<T> list = new ObjectArrayList<>();
        list.add(obj);
        lists.add(list);
    }

    @SuppressWarnings("unchecked")
    public ImmutableList<T> build() {
        int size = 0;
        for (ObjectArrayList<T> list : lists) {
            size += list.size();
        }

        T[] array = (T[]) new Object[size];
        int destPos = 0;
        for (ObjectArrayList<T> list : lists) {
            System.arraycopy(list.elements(), 0, array, destPos, list.size());
            destPos += list.size();
            list.clear();
        }

        return ImmutableList.copyOf(array);
    }
}
