package com.zurrtum.create.content.logistics.packager.repackager;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.logistics.packager.PackagerBlock;
import com.zurrtum.create.content.logistics.packager.PackagerBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.state.StateManager;

public class RepackagerBlock extends PackagerBlock {

    public RepackagerBlock(Settings properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends PackagerBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.REPACKAGER;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

}
