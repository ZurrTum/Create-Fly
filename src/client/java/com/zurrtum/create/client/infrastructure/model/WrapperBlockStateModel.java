package com.zurrtum.create.client.infrastructure.model;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class WrapperBlockStateModel implements BlockStateModel, BlockStateModel.UnbakedRoot {
    private static final RandomSource RANDOM = RandomSource.createThreadLocalInstance(0L);
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
        BlockPos.MutableBlockPos position = pos.mutable();
        for (Vec3i move : DIRECTIONS) {
            target = level.getBlockState(position.setWithOffset(pos, move));
            if (target == state) {
                return position;
            }
        }
        return pos;
    }

    @Nullable
    public static BlockStateModel getBlockDestroyModel(
        BlockStateModel model,
        BlockAndTintGetter level,
        BlockPos pos,
        BlockState state,
        long seed
    ) {
        if (model instanceof WrapperBlockStateModel wrapper) {
            return extractBlockDestroyModel(wrapper, level, pos, state, seed);
        }
        return null;
    }

    @Nullable
    public static BlockStateModel getBlockDestroyModel(
        BlockStateModel model,
        BlockAndTintGetter level,
        BlockPos pos,
        BlockState state
    ) {
        if (model instanceof WrapperBlockStateModel wrapper) {
            return extractBlockDestroyModel(wrapper, level, pos, state, state.getSeed(pos));
        }
        return null;
    }

    private static BlockStateModel extractBlockDestroyModel(
        WrapperBlockStateModel model,
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState state,
        long seed
    ) {
        RANDOM.setSeed(seed);
        ArrayList<BlockStateModelPart> parts = new ArrayList<>();
        model.addPartsWithInfo(world, pos, state, RANDOM, parts);
        return switch (parts.size()) {
            case 0 -> BlockStateRenderModel.INSTANCE;
            case 1 -> new BlockStateRenderModel.Single(parts.getFirst());
            default -> new BlockStateRenderModel.Multiple(parts);
        };
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
        private static final BlockStateModel INSTANCE = new BlockStateRenderModel();

        @Override
        public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
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

        private static class Single extends BlockStateRenderModel {
            private final BlockStateModelPart part;

            private Single(BlockStateModelPart part) {
                this.part = part;
            }

            @Override
            public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
                output.add(part);
            }
        }

        private static class Multiple extends BlockStateRenderModel {
            private final List<BlockStateModelPart> parts;

            private Multiple(List<BlockStateModelPart> parts) {
                this.parts = parts;
            }

            @Override
            public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
                output.addAll(parts);
            }
        }
    }
}
