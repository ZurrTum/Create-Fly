package com.zurrtum.create.foundation.blockEntity;

import net.minecraft.structure.StructureTemplate;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class StructureEntityInfoIterator implements Iterator<StructureTemplate.StructureEntityInfo> {
    private final World world;
    private final List<EntityControlStructureProcessor> controls;
    private Iterator<StructureTemplate.StructureEntityInfo> iterator;
    private StructureTemplate.StructureEntityInfo next;

    public StructureEntityInfoIterator(
        World world,
        List<EntityControlStructureProcessor> controls,
        Iterator<StructureTemplate.StructureEntityInfo> iterator
    ) {
        this.world = world;
        this.controls = controls;
        this.iterator = iterator;
    }

    private boolean test(StructureTemplate.StructureEntityInfo info) {
        for (EntityControlStructureProcessor processor : controls) {
            if (processor.skip(world, info)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasNext() {
        if (next != null) {
            return true;
        }
        if (iterator == null) {
            return false;
        }
        while (iterator.hasNext()) {
            StructureTemplate.StructureEntityInfo info = iterator.next();
            if (test(info)) {
                next = info;
                return true;
            }
        }
        iterator = null;
        return false;
    }

    @Override
    public StructureTemplate.StructureEntityInfo next() {
        if (hasNext()) {
            StructureTemplate.StructureEntityInfo result = next;
            next = null;
            return result;
        }
        throw new NoSuchElementException();
    }
}
