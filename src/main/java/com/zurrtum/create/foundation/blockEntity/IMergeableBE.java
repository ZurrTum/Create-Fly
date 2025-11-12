package com.zurrtum.create.foundation.blockEntity;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface IMergeableBE {

    void accept(BlockEntity other);

}
