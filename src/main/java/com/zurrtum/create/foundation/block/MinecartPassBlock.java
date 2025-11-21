package com.zurrtum.create.foundation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface MinecartPassBlock {
    void onMinecartPass(BlockState state, Level world, BlockPos pos, AbstractMinecart cart);
}
