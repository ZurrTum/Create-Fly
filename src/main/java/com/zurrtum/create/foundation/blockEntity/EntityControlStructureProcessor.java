package com.zurrtum.create.foundation.blockEntity;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public interface EntityControlStructureProcessor {
    boolean skip(Level world, StructureTemplate.StructureEntityInfo info);
}
