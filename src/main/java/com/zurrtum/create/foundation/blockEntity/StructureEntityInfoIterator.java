package com.zurrtum.create.foundation.blockEntity;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureEntityInfo;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class StructureEntityInfoIterator implements Iterator<StructureEntityInfo> {
    private final Level world;
    private final List<EntityControlStructureProcessor> controls;
    private @Nullable Iterator<StructureEntityInfo> iterator;
    private @Nullable StructureEntityInfo next;

    public StructureEntityInfoIterator(Level world, List<EntityControlStructureProcessor> controls, Iterator<StructureEntityInfo> iterator) {
        this.world = world;
        this.controls = controls;
        this.iterator = iterator;
    }

    private boolean test(StructureEntityInfo info) {
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
            StructureEntityInfo info = iterator.next();
            if (test(info)) {
                next = info;
                return true;
            }
        }
        iterator = null;
        return false;
    }

    @Override
    public StructureEntityInfo next() {
        if (hasNext()) {
            assert next != null;
            StructureEntityInfo result = next;
            next = null;
            return result;
        }
        throw new NoSuchElementException();
    }
}
