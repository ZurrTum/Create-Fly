package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllCTBehaviours;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.content.decoration.girder.GirderBlock;
import java.util.List;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class ConnectedGirderModel extends CTModel {
    public ConnectedGirderModel(BlockState state, UnbakedRoot unbaked) {
        super(state, unbaked, AllCTBehaviours.METAL_GIRDER);
    }

    @Override
    public void addPartsWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts) {
        super.addPartsWithInfo(world, pos, state, random, parts);
        for (Direction direction : Iterate.horizontalDirections) {
            if (GirderBlock.isConnected(world, pos, state, direction)) {
                parts.add(AllPartialModels.METAL_GIRDER_BRACKETS.get(direction).get());
            }
        }
    }
}
