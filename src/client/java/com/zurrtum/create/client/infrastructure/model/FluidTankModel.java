package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.api.connectivity.ConnectivityHandler;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllCTBehaviours;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class FluidTankModel extends CTModel {
    public FluidTankModel(BlockState state, UnbakedRoot unbaked, ConnectedTextureBehaviour behaviour) {
        super(state, unbaked, behaviour);
    }

    public static FluidTankModel standard(BlockState state, UnbakedRoot unbaked) {
        return new FluidTankModel(state, unbaked, AllCTBehaviours.FLUID_TANK);
    }

    public static FluidTankModel creative(BlockState state, UnbakedRoot unbaked) {
        return new FluidTankModel(state, unbaked, AllCTBehaviours.CREATIVE_FLUID_TANK);
    }

    @Override
    public void addPartsWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts) {
        int[] indices = createCTData(world, pos, state);
        boolean[] culls = createCullData(world, pos);
        for (BlockModelPart part : model.collectParts(random)) {
            QuadCollection.Builder builder = new QuadCollection.Builder();
            for (BakedQuad quad : part.getQuads(null)) {
                builder.addUnculledFace(replaceQuad(state, random, indices[quad.direction().get3DDataValue()], quad));
            }
            for (Direction direction : Iterate.directions) {
                int i = direction.get2DDataValue();
                if (i != -1 && culls[i]) {
                    continue;
                }
                addQuads(builder, part, direction, state, random, indices[direction.get3DDataValue()]);
            }
            parts.add(new SimpleModelWrapper(builder.build(), part.useAmbientOcclusion(), part.particleIcon()));
        }
    }

    protected boolean[] createCullData(BlockAndTintGetter world, BlockPos pos) {
        boolean[] culledFaces = new boolean[4];
        for (Direction face : Iterate.horizontalDirections) {
            culledFaces[face.get2DDataValue()] = ConnectivityHandler.isConnected(world, pos, pos.relative(face));
        }
        return culledFaces;
    }
}
