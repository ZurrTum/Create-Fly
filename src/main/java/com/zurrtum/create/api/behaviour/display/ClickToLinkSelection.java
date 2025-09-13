package com.zurrtum.create.api.behaviour.display;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public interface ClickToLinkSelection {
    Box getSelectionBounds(World world, BlockPos pos);
}
