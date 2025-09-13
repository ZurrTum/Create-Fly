package com.zurrtum.create.content.fluids.tank.storage;

import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.zurrtum.create.content.fluids.tank.FluidTankBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FluidTankMountedStorageType extends MountedFluidStorageType<FluidTankMountedStorage> {
    public FluidTankMountedStorageType() {
        super(FluidTankMountedStorage.CODEC);
    }

    @Override
    @Nullable
    public FluidTankMountedStorage mount(World level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof FluidTankBlockEntity tank && tank.isController()) {
            return FluidTankMountedStorage.fromTank(tank);
        }

        return null;
    }
}