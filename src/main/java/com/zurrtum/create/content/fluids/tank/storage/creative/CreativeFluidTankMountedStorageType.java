package com.zurrtum.create.content.fluids.tank.storage.creative;

import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.zurrtum.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CreativeFluidTankMountedStorageType extends MountedFluidStorageType<CreativeFluidTankMountedStorage> {
    public CreativeFluidTankMountedStorageType() {
        super(CreativeFluidTankMountedStorage.CODEC);
    }

    @Override
    @Nullable
    public CreativeFluidTankMountedStorage mount(World level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof CreativeFluidTankBlockEntity tank) {
            return CreativeFluidTankMountedStorage.fromTank(tank);
        }

        return null;
    }
}