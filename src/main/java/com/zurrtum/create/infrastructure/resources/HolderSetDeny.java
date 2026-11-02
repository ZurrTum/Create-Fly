package com.zurrtum.create.infrastructure.resources;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class HolderSetDeny extends HolderSetExtend {
    private @Nullable List<Holder<Item>> contents;
    private @Nullable Set<Holder<Item>> contentsSet;

    public HolderSetDeny(HolderSet<Item> origin, HolderSet<Item> tag) {
        super(origin, tag);
    }

    @Override
    public boolean contains(Holder<Item> value) {
        if (contentsSet == null) {
            contentsSet = Set.copyOf(contents());
        }
        return contentsSet.contains(value);
    }

    private List<Holder<Item>> contents() {
        if (contents == null) {
            contents = new ArrayList<>(origin.size());
            for (Holder<Item> holder : origin) {
                if (tag.contains(holder)) {
                    continue;
                }
                contents.add(holder);
            }
        }
        return contents;
    }

    @Override
    public Holder<Item> get(int index) {
        return contents().get(index);
    }

    @Override
    public Iterator<Holder<Item>> iterator() {
        return contents().iterator();
    }

    @Override
    public int size() {
        return contents().size();
    }

    @Override
    public Stream<Holder<Item>> stream() {
        return contents().stream();
    }
}
