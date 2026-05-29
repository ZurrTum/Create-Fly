package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import com.zurrtum.create.content.decoration.copycat.CopycatBlockEntity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;

/**
 * If FRAPI is already loaded, you need to override {@link FabricBlockStateModel#emitQuads} to use the {@link MutableQuadView#ambientOcclusion(TriState ao)} and {@link MutableQuadView#emissive(boolean emissive)} methods.
 */
public abstract class CopycatModel extends WrapperBlockStateModel {
    protected static final AABB CUBE_AABB = new AABB(BlockPos.ZERO);
    private @UnknownNullability ModelManager modelManager;

    public CopycatModel(BlockState state, UnbakedRoot unbaked) {
        super(state, unbaked);
    }

    @Override
    public BlockStateModel bake(BlockState state, ModelBaker baker) {
        this.modelManager = Minecraft.getInstance().getModelManager();
        return super.bake(state, baker);
    }

    @Override
    public void addPartsWithInfo(
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState state,
        RandomSource random,
        List<BlockStateModelPart> parts
    ) {
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
        List<BlockStateModelPart> parts
    );

    protected BlockStateModel getModelOf(BlockState material) {
        return modelManager.getBlockStateModelSet().get(material);
    }

    @Override
    public boolean needUpdateTerrainParticle() {
        return true;
    }

    @Override
    public Material.Baked particleMaterialWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state) {
        if (world.getBlockEntity(pos) instanceof CopycatBlockEntity copycat) {
            return getModelOf(copycat.getMaterial()).particleMaterial();
        }
        return model.particleMaterial();
    }

    protected void addModelParts(
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState material,
        RandomSource random,
        BlockStateModel model,
        List<BlockStateModelPart> parts
    ) {
        if (model instanceof WrapperBlockStateModel wrapper) {
            wrapper.addPartsWithInfo(world, pos, material, random, parts);
        } else {
            model.collectParts(random, parts);
        }
    }

    protected List<BlockStateModelPart> getMaterialParts(
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState material,
        RandomSource random,
        BlockStateModel model
    ) {
        List<BlockStateModelPart> parts = new ObjectArrayList<>();
        addModelParts(world, pos, material, random, model, parts);
        return parts;
    }

    protected DirectionData gatherDirectionData(CopycatBlock block, BlockState state) {
        DirectionData directionData = new DirectionData();
        for (Direction direction : Iterate.directions) {
            if (block.shouldFaceAlwaysRender(state, direction)) {
                directionData.noCull(direction);
            }
        }
        return directionData;
    }

    protected static class DirectionData {
        private int data;

        public void cull(Direction face) {
            data &= ~(1 << face.get3DDataValue());
        }

        public void noCull(Direction face) {
            data |= (1 << face.get3DDataValue());
        }

        public boolean isCull(Direction face) {
            return (data & (1 << face.get3DDataValue())) == 0;
        }

        public boolean isUncull(Direction face) {
            return (data & (1 << face.get3DDataValue())) != 0;
        }
    }

    protected static class SpriteHolder {
        private TextureAtlasSprite sprite;
        public boolean isDefault = true;

        public SpriteHolder(TextureAtlasSprite sprite) {
            this.sprite = sprite;
        }

        public void set(TextureAtlasSprite sprite) {
            this.sprite = sprite;
            isDefault = false;
        }

        public TextureAtlasSprite get() {
            return sprite;
        }
    }
}
