package com.zurrtum.create.client.infrastructure.model;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public abstract class WrapperBlockStateModel implements BlockStateModel, BlockStateModel.UnbakedRoot {
    private static final RandomSource random = RandomSource.createThreadLocalInstance(0L);
    private static final Vec3i[] DIRECTIONS = new Vec3i[]{
        new Vec3i(0, 0, -1),
        new Vec3i(0, 0, 1),
        new Vec3i(-1, 0, 0),
        new Vec3i(1, 0, 0),
        new Vec3i(-1, 0, -1),
        new Vec3i(1, 0, -1),
        new Vec3i(1, 0, 1),
        new Vec3i(-1, 0, 1),
        new Vec3i(0, -1, 0),
        new Vec3i(0, 1, 0),
        new Vec3i(0, -1, -1),
        new Vec3i(0, -1, 1),
        new Vec3i(-1, -1, 0),
        new Vec3i(1, -1, 0),
        new Vec3i(-1, -1, -1),
        new Vec3i(1, -1, -1),
        new Vec3i(1, -1, 1),
        new Vec3i(-1, -1, 1)
    };

    public static BlockPos findPos(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        BlockState target = level.getBlockState(pos);
        if (target == state) {
            return pos;
        }
        MutableBlockPos position = new MutableBlockPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        for (Vec3i move : DIRECTIONS) {
            target = level.getBlockState(position.set(x + move.getX(), y + move.getY(), z + move.getZ()));
            if (target == state) {
                return position;
            }
        }
        return pos;
    }

    public static void addPartsWithInfo(
        BlockStateModel model,
        BlockAndTintGetter level,
        BlockPos pos,
        BlockState state,
        RandomSource random,
        List<BlockStateModelPart> parts
    ) {
        if (model instanceof WrapperBlockStateModel wrapper) {
            wrapper.addPartsWithInfo(level, pos, state, random, parts);
        } else {
            model.collectParts(random, parts);
        }
    }

    public static BlockStateModel getBlockDestroyModel(
        BlockStateModel model,
        BlockAndTintGetter level,
        BlockPos pos,
        BlockState state
    ) {
        if (model instanceof WrapperBlockStateModel wrapper) {
            return extractBlockDestroyModel(wrapper, level, pos, state, state.getSeed(pos));
        }
        return model;
    }

    private static BlockStateModel extractBlockDestroyModel(
        WrapperBlockStateModel model,
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState state,
        long seed
    ) {
        random.setSeed(seed);
        BlockStateRenderModel renderModel = BlockStateRenderModel.create();
        model.addPartsWithInfo(world, pos, state, random, renderModel.parts);
        return renderModel;
    }

    protected @UnknownNullability BlockStateModel model;
    protected @Nullable Entry entry;

    public WrapperBlockStateModel() {
    }

    public WrapperBlockStateModel(BlockState state, UnbakedRoot unbaked) {
        entry = new Entry(state, unbaked);
    }

    public void addPartsWithInfo(
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState state,
        RandomSource random,
        List<BlockStateModelPart> parts
    ) {
        collectParts(random, parts);
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
        model.collectParts(random, parts);
    }

    public boolean needUpdateTerrainParticle() {
        return false;
    }

    public Material.Baked particleMaterialWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state) {
        return particleMaterial();
    }

    @Override
    public Material.Baked particleMaterial() {
        return model.particleMaterial();
    }

    @Override
    public int materialFlags() {
        return model.materialFlags();
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

    private static class BlockStateRenderModel implements BlockStateModel {
        private static final Deque<BlockStateRenderModel> pool = new ArrayDeque<>();
        private final List<BlockStateModelPart> parts = new ObjectArrayList<>();
        private boolean recycle = true;

        public static BlockStateRenderModel create() {
            BlockStateRenderModel model = pool.pollFirst();
            if (model != null) {
                model.clear();
                return model;
            }
            return new BlockStateRenderModel();
        }

        public void clear() {
            parts.clear();
            recycle = true;
        }

        @Override
        public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
            output.addAll(parts);
            if (recycle) {
                recycle = false;
                pool.addLast(this);
            }
        }

        @Override
        @SuppressWarnings("DataFlowIssue")
        public Material.Baked particleMaterial() {
            return null;
        }

        @Override
        public int materialFlags() {
            return 0;
        }
    }
}
