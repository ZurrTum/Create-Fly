package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.api.connectivity.ConnectivityHandler;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllCTBehaviours;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedGeometry;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.GeometryBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import java.util.List;

public class FluidTankModel extends CTModel {
    public FluidTankModel(BlockState state, UnbakedGrouped unbaked, ConnectedTextureBehaviour behaviour) {
        super(state, unbaked, behaviour);
    }

    public static FluidTankModel standard(BlockState state, UnbakedGrouped unbaked) {
        return new FluidTankModel(state, unbaked, AllCTBehaviours.FLUID_TANK);
    }

    public static FluidTankModel creative(BlockState state, UnbakedGrouped unbaked) {
        return new FluidTankModel(state, unbaked, AllCTBehaviours.CREATIVE_FLUID_TANK);
    }

    @Override
    public void addPartsWithInfo(BlockRenderView world, BlockPos pos, BlockState state, Random random, List<BlockModelPart> parts) {
        int[] indices = createCTData(world, pos, state);
        boolean[] culls = createCullData(world, pos);
        for (BlockModelPart part : model.getParts(random)) {
            BakedGeometry.Builder builder = new BakedGeometry.Builder();
            for (BakedQuad quad : part.getQuads(null)) {
                builder.add(replaceQuad(state, random, indices[quad.face().getIndex()], quad));
            }
            for (Direction direction : Iterate.directions) {
                int i = direction.getHorizontalQuarterTurns();
                if (i != -1 && culls[i]) {
                    continue;
                }
                addQuads(builder, part, direction, state, random, indices[direction.getIndex()]);
            }
            parts.add(new GeometryBakedModel(builder.build(), part.useAmbientOcclusion(), part.particleSprite()));
        }
    }

    protected boolean[] createCullData(BlockRenderView world, BlockPos pos) {
        boolean[] culledFaces = new boolean[4];
        for (Direction face : Iterate.horizontalDirections) {
            culledFaces[face.getHorizontalQuarterTurns()] = ConnectivityHandler.isConnected(world, pos, pos.offset(face));
        }
        return culledFaces;
    }
}
