package com.zurrtum.create.content.fluids.tank.storage.creative;

import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.zurrtum.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CreativeFluidTankMountedStorageType extends MountedFluidStorageType<CreativeFluidTankMountedStorage> {
    public CreativeFluidTankMountedStorageType() {
        super(CreativeFluidTankMountedStorage.CODEC);
    }

    @Override
    @Nullable
    public CreativeFluidTankMountedStorage mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof CreativeFluidTankBlockEntity tank) {
            return CreativeFluidTankMountedStorage.fromTank(tank);
        }

        return null;
    }
}