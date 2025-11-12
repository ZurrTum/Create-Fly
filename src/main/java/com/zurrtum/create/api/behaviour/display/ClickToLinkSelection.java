package com.zurrtum.create.api.behaviour.display;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public interface ClickToLinkSelection {
    AABB getSelectionBounds(Level world, BlockPos pos);
}
