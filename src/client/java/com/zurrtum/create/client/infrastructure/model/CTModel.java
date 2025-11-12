package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.client.foundation.block.connected.CTType;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour.CTContext;
import com.zurrtum.create.client.foundation.model.BakedQuadHelper;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class CTModel extends WrapperBlockStateModel {
    private final ConnectedTextureBehaviour behaviour;

    public CTModel(BlockState state, UnbakedRoot unbaked, ConnectedTextureBehaviour behaviour) {
        super(state, unbaked);
        this.behaviour = behaviour;
    }

    public static BiFunction<BlockState, UnbakedRoot, UnbakedRoot> of(ConnectedTextureBehaviour behaviour) {
        return (state, unbaked) -> new CTModel(state, unbaked, behaviour);
    }

    @Override
    public void addPartsWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts) {
        int[] indices = createCTData(world, pos, state);
        for (BlockModelPart part : model.collectParts(random)) {
            QuadCollection.Builder builder = new QuadCollection.Builder();
            for (BakedQuad quad : part.getQuads(null)) {
                builder.addUnculledFace(replaceQuad(state, random, indices[quad.direction().get3DDataValue()], quad));
            }
            for (Direction direction : Iterate.directions) {
                addQuads(builder, part, direction, state, random, indices[direction.get3DDataValue()]);
            }
            parts.add(new SimpleModelWrapper(builder.build(), part.useAmbientOcclusion(), part.particleIcon()));
        }
    }

    protected void addQuads(QuadCollection.Builder builder, BlockModelPart part, Direction direction, BlockState state, RandomSource random, int index) {
        for (BakedQuad quad : part.getQuads(direction)) {
            builder.addCulledFace(direction, replaceQuad(state, random, index, quad));
        }
    }

    protected BakedQuad replaceQuad(BlockState state, RandomSource random, int index, BakedQuad quad) {
        if (index == -1) {
            return quad;
        }
        CTSpriteShiftEntry spriteShift = behaviour.getShift(state, random, quad.direction(), quad.sprite());
        if (spriteShift == null || quad.sprite() != spriteShift.getOriginal()) {
            return quad;
        }
        BakedQuad newQuad = BakedQuadHelper.clone(quad);
        int[] vertexData = newQuad.vertices();
        for (int vertex = 0; vertex < 4; vertex++) {
            float u = BakedQuadHelper.getU(vertexData, vertex);
            float v = BakedQuadHelper.getV(vertexData, vertex);
            BakedQuadHelper.setU(vertexData, vertex, spriteShift.getTargetU(u, index));
            BakedQuadHelper.setV(vertexData, vertex, spriteShift.getTargetV(v, index));
        }
        return newQuad;
    }

    protected int[] createCTData(BlockAndTintGetter world, BlockPos pos, BlockState state) {
        int[] indices = new int[6];
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (Direction face : Iterate.directions) {
            BlockState actualState = world.getBlockState(pos);
            if (!behaviour.buildContextForOccludedDirections() && !Block.shouldRenderFace(
                state,
                world.getBlockState(mutablePos.setWithOffset(pos, face)),
                face
            ) && !(actualState.getBlock() instanceof CopycatBlock ufb && !ufb.canFaceBeOccluded(actualState, face))) {
                indices[face.get3DDataValue()] = -1;
                continue;
            }
            CTType dataType = behaviour.getDataType(world, pos, state, face);
            if (dataType == null) {
                indices[face.get3DDataValue()] = -1;
                continue;
            }
            CTContext context = behaviour.buildContext(world, pos, state, face, dataType.getContextRequirement());
            indices[face.get3DDataValue()] = dataType.getTextureIndex(context);
        }
        return indices;
    }
}
