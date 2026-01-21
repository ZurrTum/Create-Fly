package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.client.compat.fabric.WrapperModel;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.List;

public abstract class WrapperBlockStateModel implements BlockStateModel, BlockStateModel.UnbakedRoot {
    private static final boolean FABRIC = FabricLoader.getInstance().isModLoaded("fabric-model-loading-api-v1");
    protected BlockStateModel model;
    protected @Nullable Entry entry;

    public WrapperBlockStateModel() {
    }

    public WrapperBlockStateModel(BlockState state, UnbakedRoot unbaked) {
        entry = new Entry(state, unbaked);
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
        if (entry != null) {
            model = entry.bake(baker);
            entry = null;
        }
        return this;
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        if (entry != null) {
            entry.resolveDependencies(resolver);
        }
    }

    @Override
    public Object visualEqualityGroup(BlockState state) {
        return this;
    }

    protected record Entry(BlockState state, UnbakedRoot unbaked) {
        public BlockStateModel bake(ModelBaker baker) {
            return unbaked.bake(state, baker);
        }

        public void resolveDependencies(Resolver resolver) {
            unbaked.resolveDependencies(resolver);
        }
    }

    public static BlockStateModel unwrapCompat(BlockStateModel model) {
        if (FABRIC) {
            while (model instanceof WrapperModel wrapper) {
                model = wrapper.create$getWrapped();
            }
        }
        return model;
    }
}
