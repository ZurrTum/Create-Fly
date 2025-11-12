package com.zurrtum.create.content.kinetics.gearbox;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.kinetics.base.DirectionalShaftHalvesBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class GearboxBlockEntity extends DirectionalShaftHalvesBlockEntity {

    public GearboxBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.GEARBOX, pos, state);
    }

    @Override
    public boolean isNoisy() {
        return false;
    }

}
