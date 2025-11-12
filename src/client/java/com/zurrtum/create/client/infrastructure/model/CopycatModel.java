package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import com.zurrtum.create.content.decoration.copycat.CopycatBlockEntity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public abstract class CopycatModel extends WrapperBlockStateModel {
    public CopycatModel(BlockState state, UnbakedRoot unbaked) {
        super(state, unbaked);
    }

    public static ChunkSectionLayer getLayer(BlockAndTintGetter world, BlockPos pos) {
        return ItemBlockRenderTypes.getChunkRenderType(CopycatBlock.getMaterial(world, pos));
    }

    public static int getColor(BlockState state, BlockAndTintGetter world, BlockPos pos, int i) {
        if (world == null || pos == null) {
            return GrassColor.getDefaultColor();
        }
        return Minecraft.getInstance().getBlockColors().getColor(CopycatBlock.getMaterial(world, pos), world, pos, i);
    }

    @Override
    public void addPartsWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts) {
        if (!(state.getBlock() instanceof CopycatBlock block)) {
            return;
        }
        CopycatBlockEntity copycat = (CopycatBlockEntity) world.getBlockEntity(pos);
        BlockState material = copycat == null ? AllBlocks.COPYCAT_BASE.defaultBlockState() : copycat.getMaterial();
        addPartsWithInfo(world, pos, state, block, material, random, parts);
    }

    protected abstract void addPartsWithInfo(
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState state,
        CopycatBlock block,
        BlockState material,
        RandomSource random,
        List<BlockModelPart> parts
    );

    protected static BlockStateModel getModelOf(BlockState material) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(material);
    }

    @Override
    public TextureAtlasSprite particleSpriteWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state) {
        CopycatBlockEntity copycat = (CopycatBlockEntity) world.getBlockEntity(pos);
        if (copycat == null) {
            return model.particleIcon();
        }
        return getModelOf(copycat.getMaterial()).particleIcon();
    }

    protected void addModelParts(
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState material,
        RandomSource random,
        BlockStateModel model,
        List<BlockModelPart> parts
    ) {
        if (WrapperBlockStateModel.unwrapCompat(model) instanceof WrapperBlockStateModel wrapper) {
            wrapper.addPartsWithInfo(world, pos, material, random, parts);
        } else {
            model.collectParts(random, parts);
        }
    }

    protected List<BlockModelPart> getMaterialParts(
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState material,
        RandomSource random,
        BlockStateModel model
    ) {
        List<BlockModelPart> parts = new ObjectArrayList<>();
        addModelParts(world, pos, material, random, model, parts);
        return parts;
    }

    protected OcclusionData gatherOcclusionData(
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState state,
        BlockState material,
        CopycatBlock copycatBlock
    ) {
        OcclusionData occlusionData = new OcclusionData();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (Direction face : Iterate.directions) {
            if (!copycatBlock.canFaceBeOccluded(state, face))
                continue;
            if (!Block.shouldRenderFace(material, world.getBlockState(mutablePos.setWithOffset(pos, face)), face))
                occlusionData.occlude(face);
        }
        return occlusionData;
    }

    protected static class OcclusionData {
        private final boolean[] occluded;

        public OcclusionData() {
            occluded = new boolean[6];
        }

        public void occlude(Direction face) {
            occluded[face.get3DDataValue()] = true;
        }

        public boolean isOccluded(Direction face) {
            return face != null && occluded[face.get3DDataValue()];
        }
    }
}