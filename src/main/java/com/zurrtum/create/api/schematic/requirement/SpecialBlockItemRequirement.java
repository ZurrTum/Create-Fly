package com.zurrtum.create.api.schematic.requirement;

import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public interface SpecialBlockItemRequirement {
    ItemRequirement getRequiredItems(BlockState state, @Nullable BlockEntity blockEntity);
}
