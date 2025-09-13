package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.client.foundation.block.connected.CTType;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour.CTContext;
import com.zurrtum.create.client.foundation.model.BakedQuadHelper;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import net.minecraft.block.Block;
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
import java.util.function.BiFunction;

public class CTModel extends WrapperBlockStateModel {
    private final ConnectedTextureBehaviour behaviour;

    public CTModel(BlockState state, UnbakedGrouped unbaked, ConnectedTextureBehaviour behaviour) {
        super(state, unbaked);
        this.behaviour = behaviour;
    }

    public static BiFunction<BlockState, UnbakedGrouped, UnbakedGrouped> of(ConnectedTextureBehaviour behaviour) {
        return (state, unbaked) -> new CTModel(state, unbaked, behaviour);
    }

    @Override
    public void addPartsWithInfo(BlockRenderView world, BlockPos pos, BlockState state, Random random, List<BlockModelPart> parts) {
        int[] indices = createCTData(world, pos, state);
        for (BlockModelPart part : model.getParts(random)) {
            BakedGeometry.Builder builder = new BakedGeometry.Builder();
            for (BakedQuad quad : part.getQuads(null)) {
                builder.add(replaceQuad(state, random, indices[quad.face().getIndex()], quad));
            }
            for (Direction direction : Iterate.directions) {
                addQuads(builder, part, direction, state, random, indices[direction.getIndex()]);
            }
            parts.add(new GeometryBakedModel(builder.build(), part.useAmbientOcclusion(), part.particleSprite()));
        }
    }

    protected void addQuads(BakedGeometry.Builder builder, BlockModelPart part, Direction direction, BlockState state, Random random, int index) {
        for (BakedQuad quad : part.getQuads(direction)) {
            builder.add(direction, replaceQuad(state, random, index, quad));
        }
    }

    protected BakedQuad replaceQuad(BlockState state, Random random, int index, BakedQuad quad) {
        if (index == -1) {
            return quad;
        }
        CTSpriteShiftEntry spriteShift = behaviour.getShift(state, random, quad.face(), quad.sprite());
        if (spriteShift == null || quad.sprite() != spriteShift.getOriginal()) {
            return quad;
        }
        BakedQuad newQuad = BakedQuadHelper.clone(quad);
        int[] vertexData = newQuad.vertexData();
        for (int vertex = 0; vertex < 4; vertex++) {
            float u = BakedQuadHelper.getU(vertexData, vertex);
            float v = BakedQuadHelper.getV(vertexData, vertex);
            BakedQuadHelper.setU(vertexData, vertex, spriteShift.getTargetU(u, index));
            BakedQuadHelper.setV(vertexData, vertex, spriteShift.getTargetV(v, index));
        }
        return newQuad;
    }

    protected int[] createCTData(BlockRenderView world, BlockPos pos, BlockState state) {
        int[] indices = new int[6];
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        for (Direction face : Iterate.directions) {
            BlockState actualState = world.getBlockState(pos);
            if (!behaviour.buildContextForOccludedDirections() && !Block.shouldDrawSide(
                state,
                world.getBlockState(mutablePos.set(pos, face)),
                face
            ) && !(actualState.getBlock() instanceof CopycatBlock ufb && !ufb.canFaceBeOccluded(actualState, face))) {
                indices[face.getIndex()] = -1;
                continue;
            }
            CTType dataType = behaviour.getDataType(world, pos, state, face);
            if (dataType == null) {
                indices[face.getIndex()] = -1;
                continue;
            }
            CTContext context = behaviour.buildContext(world, pos, state, face, dataType.getContextRequirement());
            indices[face.getIndex()] = dataType.getTextureIndex(context);
        }
        return indices;
    }
}
