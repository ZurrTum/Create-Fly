package com.zurrtum.create.api.equipment.goggles;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Implement this interface on the {@link BlockEntity} that wants proxy the information
 */
public interface IProxyHoveringInformation {
    BlockPos getInformationSource(World level, BlockPos pos, BlockState state);
}
