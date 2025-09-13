package com.zurrtum.create.api.schematic.requirement;

import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public interface SpecialBlockItemRequirement {
    ItemRequirement getRequiredItems(BlockState state, @Nullable BlockEntity blockEntity);
}
