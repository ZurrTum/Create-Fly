package com.zurrtum.create.foundation.blockEntity;

import net.minecraft.structure.StructureTemplate;
import net.minecraft.world.World;

public interface EntityControlStructureProcessor {
    boolean skip(World world, StructureTemplate.StructureEntityInfo info);
}
