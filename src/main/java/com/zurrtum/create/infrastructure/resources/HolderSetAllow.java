package com.zurrtum.create.infrastructure.resources;

import com.google.common.collect.Iterators;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;

import java.util.Iterator;
import java.util.stream.Stream;

public class HolderSetAllow extends HolderSetExtend {
    public HolderSetAllow(HolderSet<Item> origin, HolderSet<Item> tag) {
        super(origin, tag);
    }

    @Override
    public boolean contains(Holder<Item> value) {
        return origin.contains(value) || tag.contains(value);
    }

    @Override
    public Holder<Item> get(int index) {
        int size = origin.size();
        return index < size ? origin.get(index) : tag.get(index - size);
    }

    @Override
    public Iterator<Holder<Item>> iterator() {
        return Iterators.concat(origin.iterator(), tag.iterator());
    }

    @Override
    public int size() {
        return origin.size() + tag.size();
    }

    @Override
    public Stream<Holder<Item>> stream() {
        return Stream.concat(origin.stream(), tag.stream());
    }
}
