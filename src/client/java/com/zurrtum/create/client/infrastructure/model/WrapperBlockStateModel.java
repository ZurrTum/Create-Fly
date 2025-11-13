package com.zurrtum.create.client.infrastructure.model;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Function;

public abstract class WrapperBlockStateModel implements BlockStateModel, BlockStateModel.UnbakedRoot {
    private static final boolean FABRIC = FabricLoader.getInstance().isModLoaded("fabric-model-loading-api-v1");
    protected BlockStateModel model;
    protected Function<ModelBaker, BlockStateModel> bake;

    public WrapperBlockStateModel() {
    }

    public WrapperBlockStateModel(BlockState state, UnbakedRoot unbaked) {
        bake = baker -> unbaked.bake(state, baker);
    }

    public void addPartsWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts) {
        collectParts(random, parts);
    }

    @Override
    public void collectParts(RandomSource random, List<BlockModelPart> parts) {
        model.collectParts(random, parts);
    }

    public TextureAtlasSprite particleSpriteWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state) {
        return particleIcon();
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return model.particleIcon();
    }

    @Override
    public BlockStateModel bake(BlockState state, ModelBaker baker) {
        if (bake != null) {
            model = bake.apply(baker);
            bake = null;
        }
        return this;
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
    }

    @Override
    public Object visualEqualityGroup(BlockState state) {
        return this;
    }

    public static BlockStateModel unwrapCompat(BlockStateModel model) {
        //        if (FABRIC && model instanceof WrapperModel wrapper) {
        //            return wrapper.create$getWrapped();
        //        }
        return model;
    }
}
