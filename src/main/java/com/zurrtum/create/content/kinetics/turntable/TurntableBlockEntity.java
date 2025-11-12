package com.zurrtum.create.content.kinetics.turntable;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TurntableBlockEntity extends KineticBlockEntity {
    public TurntableBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.TURNTABLE, pos, state);
    }
}
