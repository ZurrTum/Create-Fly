package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.client.compat.fabric.WrapperModel;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import java.util.List;
import java.util.function.Function;

public abstract class WrapperBlockStateModel implements BlockStateModel, BlockStateModel.UnbakedGrouped {
    private static final boolean FABRIC = FabricLoader.getInstance().isModLoaded("fabric-model-loading-api-v1");
    protected BlockStateModel model;
    protected Function<Baker, BlockStateModel> bake;

    public WrapperBlockStateModel() {
    }

    public WrapperBlockStateModel(BlockState state, UnbakedGrouped unbaked) {
        bake = baker -> unbaked.bake(state, baker);
    }

    public void addPartsWithInfo(BlockRenderView world, BlockPos pos, BlockState state, Random random, List<BlockModelPart> parts) {
        addParts(random, parts);
    }

    @Override
    public void addParts(Random random, List<BlockModelPart> parts) {
        model.addParts(random, parts);
    }

    public Sprite particleSpriteWithInfo(BlockRenderView world, BlockPos pos, BlockState state) {
        return particleSprite();
    }

    @Override
    public Sprite particleSprite() {
        return model.particleSprite();
    }

    @Override
    public BlockStateModel bake(BlockState state, Baker baker) {
        if (bake != null) {
            model = bake.apply(baker);
            bake = null;
        }
        return this;
    }

    @Override
    public void resolve(Resolver resolver) {
    }

    @Override
    public Object getEqualityGroup(BlockState state) {
        return this;
    }

    public static BlockStateModel unwrapCompat(BlockStateModel model) {
        if (FABRIC && model instanceof WrapperModel wrapper) {
            return wrapper.create$getWrapped();
        }
        return model;
    }
}
