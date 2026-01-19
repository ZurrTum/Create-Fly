package com.zurrtum.create.content.fluids.tank.storage;

import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.zurrtum.create.content.fluids.tank.FluidTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class FluidTankMountedStorageType extends MountedFluidStorageType<FluidTankMountedStorage> {
    public FluidTankMountedStorageType() {
        super(FluidTankMountedStorage.CODEC);
    }

    @Override
    @Nullable
    public FluidTankMountedStorage mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof FluidTankBlockEntity tank && tank.isController()) {
            return FluidTankMountedStorage.fromTank(tank);
        }

        return null;
    }
}