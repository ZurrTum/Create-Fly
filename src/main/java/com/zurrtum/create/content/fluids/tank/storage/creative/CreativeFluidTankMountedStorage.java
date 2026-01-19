package com.zurrtum.create.content.fluids.tank.storage.creative;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllMountedStorageTypes;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.zurrtum.create.api.contraption.storage.fluid.WrapperMountedFluidStorage;
import com.zurrtum.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.zurrtum.create.content.fluids.tank.CreativeFluidTankBlockEntity.CreativeFluidTankInventory;
import com.zurrtum.create.foundation.fluid.FluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class CreativeFluidTankMountedStorage extends WrapperMountedFluidStorage<CreativeFluidTankInventory> {
    public static final MapCodec<CreativeFluidTankMountedStorage> CODEC = CreativeFluidTankInventory.CODEC.xmap(
        CreativeFluidTankMountedStorage::new,
        storage -> storage.wrapped
    ).fieldOf("value");

    protected CreativeFluidTankMountedStorage(MountedFluidStorageType<?> type, CreativeFluidTankInventory tank) {
        super(type, tank);
    }

    protected CreativeFluidTankMountedStorage(CreativeFluidTankInventory tank) {
        this(AllMountedStorageTypes.CREATIVE_FLUID_TANK, tank);
    }

    @Override
    public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        // no need to do anything, supplied stack can't change while mounted
    }

    public static CreativeFluidTankMountedStorage fromTank(CreativeFluidTankBlockEntity tank) {
        // make an isolated copy
        FluidTank inv = tank.getTankInventory();
        CreativeFluidTankInventory copy = new CreativeFluidTankInventory(
            inv.size(), $ -> {
        }
        );
        copy.setFluid(inv.getFluid());
        return new CreativeFluidTankMountedStorage(copy);
    }
}