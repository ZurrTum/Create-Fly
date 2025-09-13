package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllCTBehaviours;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.content.decoration.girder.GirderBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import java.util.List;

public class ConnectedGirderModel extends CTModel {
    public ConnectedGirderModel(BlockState state, UnbakedGrouped unbaked) {
        super(state, unbaked, AllCTBehaviours.METAL_GIRDER);
    }

    @Override
    public void addPartsWithInfo(BlockRenderView world, BlockPos pos, BlockState state, Random random, List<BlockModelPart> parts) {
        super.addPartsWithInfo(world, pos, state, random, parts);
        for (Direction direction : Iterate.horizontalDirections) {
            if (GirderBlock.isConnected(world, pos, state, direction)) {
                parts.add(AllPartialModels.METAL_GIRDER_BRACKETS.get(direction).get());
            }
        }
    }
}
