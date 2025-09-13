package com.zurrtum.create.content.equipment.bell;

import com.zurrtum.create.AllBlockEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class PeculiarBellBlockEntity extends AbstractBellBlockEntity {
    public PeculiarBellBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.PECULIAR_BELL, pos, state);
    }
}
