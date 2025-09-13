package com.zurrtum.create.content.trains.track;

import com.zurrtum.create.AllTrackMaterials;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.trains.graph.TrackNodeLocation;
import com.zurrtum.create.content.trains.graph.TrackNodeLocation.DiscoveredLocation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface ITrackBlock {

    Vec3d getUpNormal(BlockView world, BlockPos pos, BlockState state);

    List<Vec3d> getTrackAxes(BlockView world, BlockPos pos, BlockState state);

    Vec3d getCurveStart(BlockView world, BlockPos pos, BlockState state, Vec3d axis);

    default int getYOffsetAt(BlockView world, BlockPos pos, BlockState state, Vec3d end) {
        return 0;
    }

    BlockState getBogeyAnchor(BlockView world, BlockPos pos, BlockState state); // should be on bogey side

    boolean trackEquals(BlockState state1, BlockState state2);

    default BlockState overlay(BlockView world, BlockPos pos, BlockState existing, BlockState placed) {
        return existing;
    }

    default double getElevationAtCenter(BlockView world, BlockPos pos, BlockState state) {
        return isSlope(world, pos, state) ? .5 : 0;
    }

    static Collection<DiscoveredLocation> walkConnectedTracks(BlockView worldIn, TrackNodeLocation location, boolean linear) {
        BlockView world = location != null && worldIn instanceof ServerWorld sl ? sl.getServer().getWorld(location.dimension) : worldIn;
        List<DiscoveredLocation> list = new ArrayList<>();
        for (BlockPos blockPos : location.allAdjacent()) {
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.getBlock() instanceof ITrackBlock track)
                list.addAll(track.getConnected(world, blockPos, blockState, linear, location));
        }
        return list;
    }

    default Collection<DiscoveredLocation> getConnected(
        BlockView worldIn,
        BlockPos pos,
        BlockState state,
        boolean linear,
        @Nullable TrackNodeLocation connectedTo
    ) {
        BlockView world = connectedTo != null && worldIn instanceof ServerWorld sl ? sl.getServer().getWorld(connectedTo.dimension) : worldIn;
        Vec3d center = Vec3d.ofBottomCenter(pos).add(0, getElevationAtCenter(world, pos, state), 0);
        List<DiscoveredLocation> list = new ArrayList<>();
        TrackShape shape = state.get(TrackBlock.SHAPE);
        List<Vec3d> trackAxes = getTrackAxes(world, pos, state);

        trackAxes.forEach(axis -> {
            BiFunction<Double, Boolean, Vec3d> offsetFactory = (d, b) -> axis.multiply(b ? d : -d).add(center);
            Function<Boolean, RegistryKey<World>> dimensionFactory = b -> world instanceof World l ? l.getRegistryKey() : World.OVERWORLD;
            Function<Vec3d, Integer> yOffsetFactory = v -> getYOffsetAt(world, pos, state, v);

            addToListIfConnected(
                connectedTo,
                list,
                offsetFactory,
                b -> shape.getNormal(),
                dimensionFactory,
                yOffsetFactory,
                axis,
                null,
                (b, v) -> getMaterialSimple(world, v)
            );
        });

        return list;
    }

    static TrackMaterial getMaterialSimple(BlockView world, Vec3d pos) {
        return getMaterialSimple(world, pos, AllTrackMaterials.ANDESITE);
    }

    static TrackMaterial getMaterialSimple(BlockView world, Vec3d pos, TrackMaterial defaultMaterial) {
        if (defaultMaterial == null)
            defaultMaterial = AllTrackMaterials.ANDESITE;
        if (world != null) {
            Block block = world.getBlockState(BlockPos.ofFloored(pos)).getBlock();
            if (block instanceof ITrackBlock track) {
                return track.getMaterial();
            }
        }
        return defaultMaterial;
    }

    static void addToListIfConnected(
        @Nullable TrackNodeLocation fromEnd,
        Collection<DiscoveredLocation> list,
        BiFunction<Double, Boolean, Vec3d> offsetFactory,
        Function<Boolean, Vec3d> normalFactory,
        Function<Boolean, RegistryKey<World>> dimensionFactory,
        Function<Vec3d, Integer> yOffsetFactory,
        Vec3d axis,
        BezierConnection viaTurn,
        BiFunction<Boolean, Vec3d, TrackMaterial> materialFactory
    ) {

        Vec3d firstOffset = offsetFactory.apply(0.5d, true);
        DiscoveredLocation firstLocation = new DiscoveredLocation(dimensionFactory.apply(true), firstOffset).viaTurn(viaTurn)
            .materialA(materialFactory.apply(true, offsetFactory.apply(0.0d, true)))
            .materialB(materialFactory.apply(true, offsetFactory.apply(1.0d, true))).withNormal(normalFactory.apply(true)).withDirection(axis)
            .withYOffset(yOffsetFactory.apply(firstOffset));

        Vec3d secondOffset = offsetFactory.apply(0.5d, false);
        DiscoveredLocation secondLocation = new DiscoveredLocation(dimensionFactory.apply(false), secondOffset).viaTurn(viaTurn)
            .materialA(materialFactory.apply(false, offsetFactory.apply(0.0d, false)))
            .materialB(materialFactory.apply(false, offsetFactory.apply(1.0d, false))).withNormal(normalFactory.apply(false)).withDirection(axis)
            .withYOffset(yOffsetFactory.apply(secondOffset));

        if (!firstLocation.dimension.equals(secondLocation.dimension)) {
            firstLocation.forceNode();
            secondLocation.forceNode();
        }

        boolean skipFirst = false;
        boolean skipSecond = false;

        if (fromEnd != null) {
            boolean equalsFirst = firstLocation.equals(fromEnd);
            boolean equalsSecond = secondLocation.equals(fromEnd);

            // not reachable from this end
            if (!equalsFirst && !equalsSecond)
                return;

            if (equalsFirst)
                skipFirst = true;
            if (equalsSecond)
                skipSecond = true;
        }

        if (!skipFirst)
            list.add(firstLocation);
        if (!skipSecond)
            list.add(secondLocation);
    }

    default boolean isSlope(BlockView world, BlockPos pos, BlockState state) {
        return getTrackAxes(world, pos, state).get(0).y != 0;
    }

    default Pair<Vec3d, AxisDirection> getNearestTrackAxis(BlockView world, BlockPos pos, BlockState state, Vec3d lookVec) {
        Vec3d best = null;
        double bestDiff = Double.MAX_VALUE;
        for (Vec3d vec3 : getTrackAxes(world, pos, state)) {
            for (int opposite : Iterate.positiveAndNegative) {
                double distanceTo = vec3.normalize().distanceTo(lookVec.multiply(opposite));
                if (distanceTo > bestDiff)
                    continue;
                bestDiff = distanceTo;
                best = vec3;
            }
        }
        return Pair.of(best, lookVec.dotProduct(best.multiply(1, 0, 1).normalize()) < 0 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE);
    }

    TrackMaterial getMaterial();

}
