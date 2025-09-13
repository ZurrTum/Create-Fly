package com.zurrtum.create.foundation.block;

import net.minecraft.block.BlockState;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface MinecartPassBlock {
    void onMinecartPass(BlockState state, World world, BlockPos pos, AbstractMinecartEntity cart);
}
