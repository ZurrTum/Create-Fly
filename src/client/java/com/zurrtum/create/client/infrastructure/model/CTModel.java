package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.client.foundation.block.connected.CTType;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.BiFunction;

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
    public void addPartsWithInfo(
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState state,
        RandomSource random,
        List<BlockStateModelPart> parts
    ) {
        int[] indices = createCTData(world, pos, state);
        List<BlockStateModelPart> modelParts = new ObjectArrayList<>();
        model.collectParts(random, modelParts);
        for (BlockStateModelPart part : modelParts) {
            QuadCollection.Builder builder = new QuadCollection.Builder();
            for (BakedQuad quad : part.getQuads(null)) {
                builder.addUnculledFace(replaceQuad(state, random, indices[quad.direction().get3DDataValue()], quad));
            }
            for (Direction direction : Iterate.directions) {
                addQuads(builder, part, direction, state, random, indices[direction.get3DDataValue()]);
            }
            parts.add(new SimpleModelWrapper(builder.build(), part.useAmbientOcclusion(), part.particleMaterial()));
        }
    }

    protected void addQuads(
        QuadCollection.Builder builder,
        BlockStateModelPart part,
        Direction direction,
        BlockState state,
        RandomSource random,
        int index
    ) {
        for (BakedQuad quad : part.getQuads(direction)) {
            builder.addCulledFace(direction, replaceQuad(state, random, index, quad));
        }
    }

    private static long calcSpriteUv(long packedUv, CTSpriteShiftEntry spriteShift, int index) {
        float u = UVPair.unpackU(packedUv);
        float v = UVPair.unpackV(packedUv);
        return UVPair.pack(spriteShift.getTargetU(u, index), spriteShift.getTargetV(v, index));
    }

    protected BakedQuad replaceQuad(BlockState state, RandomSource random, int index, BakedQuad quad) {
        if (index == -1) {
            return quad;
        }
        MaterialInfo info = quad.materialInfo();
        TextureAtlasSprite sprite = info.sprite();
        CTSpriteShiftEntry spriteShift = behaviour.getShift(state, random, quad.direction(), sprite);
        if (spriteShift == null || sprite != spriteShift.getOriginal()) {
            return quad;
        }
        return BakedModelHelper.replaceBakedQuadUV(
            quad,
            calcSpriteUv(quad.packedUV0(), spriteShift, index),
            calcSpriteUv(quad.packedUV1(), spriteShift, index),
            calcSpriteUv(quad.packedUV2(), spriteShift, index),
            calcSpriteUv(quad.packedUV3(), spriteShift, index),
            info
        );
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
            int context = behaviour.buildContext(world, pos, state, face, dataType);
            indices[face.get3DDataValue()] = dataType.getTextureIndex(context);
        }
        return indices;
    }
}
