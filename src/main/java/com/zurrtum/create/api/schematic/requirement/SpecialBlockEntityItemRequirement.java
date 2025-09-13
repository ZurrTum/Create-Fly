package com.zurrtum.create.api.schematic.requirement;

import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import net.minecraft.block.BlockState;

public interface SpecialBlockEntityItemRequirement {
    ItemRequirement getRequiredItems(BlockState state);
}
