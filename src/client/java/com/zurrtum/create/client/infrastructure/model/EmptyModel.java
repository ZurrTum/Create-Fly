package com.zurrtum.create.client.infrastructure.model;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import java.util.List;

public class EmptyModel extends WrapperBlockStateModel {
    public EmptyModel(BlockState state, UnbakedGrouped unbaked) {
        super(state, unbaked);
    }

    @Override
    public void addPartsWithInfo(BlockRenderView world, BlockPos pos, BlockState state, Random random, List<BlockModelPart> parts) {
    }
}
