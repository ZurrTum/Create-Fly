package com.zurrtum.create.foundation.blockEntity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SmartBlockEntityTicker<T extends BlockEntity> implements BlockEntityTicker<T> {

    @Override
    public void tick(World p_155253_, BlockPos p_155254_, BlockState p_155255_, T p_155256_) {
        if (!p_155256_.hasWorld())
            p_155256_.setWorld(p_155253_);
        ((SmartBlockEntity) p_155256_).tick();
    }

}
