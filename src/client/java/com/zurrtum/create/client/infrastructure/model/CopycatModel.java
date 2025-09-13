package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import com.zurrtum.create.content.decoration.copycat.CopycatBlockEntity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.GrassColors;

import java.util.List;

public abstract class CopycatModel extends WrapperBlockStateModel {
    public CopycatModel(BlockState state, UnbakedGrouped unbaked) {
        super(state, unbaked);
    }

    public static BlockRenderLayer getLayer(BlockRenderView world, BlockPos pos) {
        return RenderLayers.getBlockLayer(CopycatBlock.getMaterial(world, pos));
    }

    public static int getColor(BlockState state, BlockRenderView world, BlockPos pos, int i) {
        if (world == null || pos == null) {
            return GrassColors.getDefaultColor();
        }
        return MinecraftClient.getInstance().getBlockColors().getColor(CopycatBlock.getMaterial(world, pos), world, pos, i);
    }

    @Override
    public void addPartsWithInfo(BlockRenderView world, BlockPos pos, BlockState state, Random random, List<BlockModelPart> parts) {
        if (!(state.getBlock() instanceof CopycatBlock block)) {
            return;
        }
        CopycatBlockEntity copycat = (CopycatBlockEntity) world.getBlockEntity(pos);
        BlockState material = copycat == null ? AllBlocks.COPYCAT_BASE.getDefaultState() : copycat.getMaterial();
        addPartsWithInfo(world, pos, state, block, material, random, parts);
    }

    protected abstract void addPartsWithInfo(
        BlockRenderView world,
        BlockPos pos,
        BlockState state,
        CopycatBlock block,
        BlockState material,
        Random random,
        List<BlockModelPart> parts
    );

    protected static BlockStateModel getModelOf(BlockState material) {
        return MinecraftClient.getInstance().getBlockRenderManager().getModel(material);
    }

    @Override
    public Sprite particleSpriteWithInfo(BlockRenderView world, BlockPos pos, BlockState state) {
        CopycatBlockEntity copycat = (CopycatBlockEntity) world.getBlockEntity(pos);
        if (copycat == null) {
            return model.particleSprite();
        }
        return getModelOf(copycat.getMaterial()).particleSprite();
    }

    protected void addModelParts(
        BlockRenderView world,
        BlockPos pos,
        BlockState material,
        Random random,
        BlockStateModel model,
        List<BlockModelPart> parts
    ) {
        if (model instanceof WrapperBlockStateModel wrapper) {
            wrapper.addPartsWithInfo(world, pos, material, random, parts);
        } else {
            model.addParts(random, parts);
        }
    }

    protected List<BlockModelPart> getMaterialParts(BlockRenderView world, BlockPos pos, BlockState material, Random random, BlockStateModel model) {
        List<BlockModelPart> parts = new ObjectArrayList<>();
        addModelParts(world, pos, material, random, model, parts);
        return parts;
    }

    protected OcclusionData gatherOcclusionData(
        BlockRenderView world,
        BlockPos pos,
        BlockState state,
        BlockState material,
        CopycatBlock copycatBlock
    ) {
        OcclusionData occlusionData = new OcclusionData();
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        for (Direction face : Iterate.directions) {
            if (!copycatBlock.canFaceBeOccluded(state, face))
                continue;
            if (!Block.shouldDrawSide(material, world.getBlockState(mutablePos.set(pos, face)), face))
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
            occluded[face.getIndex()] = true;
        }

        public boolean isOccluded(Direction face) {
            return face != null && occluded[face.getIndex()];
        }
    }
}