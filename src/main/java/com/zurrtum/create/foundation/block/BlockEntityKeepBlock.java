package com.zurrtum.create.foundation.block;

import net.minecraft.block.BlockState;

public interface BlockEntityKeepBlock {
    boolean keepBlockEntityWhenReplacedWith(BlockState state);
}
