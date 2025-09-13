package com.zurrtum.create.content.trains.track;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.trains.graph.TrackNodeLocation;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TrackBlockEntityTilt {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public Optional<Double> smoothingAngle;
    private Couple<Pair<Vec3d, Integer>> previousSmoothingHandles;

    private final TrackBlockEntity blockEntity;

    public TrackBlockEntityTilt(TrackBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        smoothingAngle = Optional.empty();
    }

    public void tryApplySmoothing() {
        if (smoothingAngle.isPresent())
            return;

        Couple<BezierConnection> discoveredSlopes = Couple.create(null, null);
        Vec3d axis = null;

        BlockState blockState = blockEntity.getCachedState();
        BlockPos worldPosition = blockEntity.getPos();
        World level = blockEntity.getWorld();

        if (!(blockState.getBlock() instanceof ITrackBlock itb))
            return;
        List<Vec3d> axes = itb.getTrackAxes(level, worldPosition, blockState);
        if (axes.size() != 1)
            return;
        if (axes.getFirst().y != 0)
            return;
        if (blockEntity.boundLocation != null)
            return;

        for (BezierConnection bezierConnection : blockEntity.connections.values()) {
            if (bezierConnection.starts.getFirst().y == bezierConnection.starts.getSecond().y)
                continue;
            Vec3d normedAxis = bezierConnection.axes.getFirst().normalize();

            if (axis != null) {
                if (discoveredSlopes.getSecond() != null)
                    return;
                if (normedAxis.dotProduct(axis) > -1 + 1 / 64.0)
                    return;
                discoveredSlopes.setSecond(bezierConnection);
                continue;
            }

            axis = normedAxis;
            discoveredSlopes.setFirst(bezierConnection);
        }

        if (discoveredSlopes.either(Objects::isNull))
            return;
        if (discoveredSlopes.getFirst().starts.getSecond().y > discoveredSlopes.getSecond().starts.getSecond().y)
            discoveredSlopes = discoveredSlopes.swap();

        Couple<Vec3d> lowStarts = discoveredSlopes.getFirst().starts;
        Couple<Vec3d> highStarts = discoveredSlopes.getSecond().starts;
        Vec3d lowestPoint = lowStarts.getSecond();
        Vec3d highestPoint = highStarts.getSecond();

        if (lowestPoint.y > lowStarts.getFirst().y)
            return;
        if (highestPoint.y < highStarts.getFirst().y)
            return;

        blockEntity.removeInboundConnections(false);
        blockEntity.connections.clear();
        TrackPropagator.onRailRemoved(level, worldPosition, blockState);

        double hDistance = discoveredSlopes.getFirst().getLength() + discoveredSlopes.getSecond().getLength();
        Vec3d baseAxis = discoveredSlopes.getFirst().axes.getFirst();
        double baseAxisLength = baseAxis.x != 0 && baseAxis.z != 0 ? Math.sqrt(2) : 1;
        double vDistance = highestPoint.y - lowestPoint.y;
        double m = vDistance / (hDistance);

        Vec3d diff = highStarts.getFirst().subtract(lowStarts.getFirst());
        boolean flipRotation = diff.dotProduct(new Vec3d(1, 0, 2).normalize()) <= 0;
        smoothingAngle = Optional.of(Math.toDegrees(MathHelper.atan2(m, 1)) * (flipRotation ? -1 : 1));

        int smoothingParam = MathHelper.clamp((int) (m * baseAxisLength * 16), 0, 15);

        Couple<Integer> smoothingResult = Couple.create(0, smoothingParam);
        Vec3d raisedOffset = diff.normalize().add(0, MathHelper.clamp(m, 0, 1 - 1 / 512.0), 0).normalize().multiply(baseAxisLength);

        highStarts.setFirst(lowStarts.getFirst().add(raisedOffset));

        boolean first = true;
        for (BezierConnection bezierConnection : discoveredSlopes) {
            int smoothingToApply = smoothingResult.get(first);

            if (bezierConnection.smoothing == null)
                bezierConnection.smoothing = Couple.create(0, 0);
            bezierConnection.smoothing.setFirst(smoothingToApply);
            bezierConnection.axes.setFirst(bezierConnection.axes.getFirst().add(0, (first ? 1 : -1) * -m, 0).normalize());

            first = false;
            BlockPos otherPosition = bezierConnection.getKey();
            BlockState otherState = level.getBlockState(otherPosition);
            if (!(otherState.getBlock() instanceof TrackBlock))
                continue;
            level.setBlockState(otherPosition, otherState.with(TrackBlock.HAS_BE, true));
            BlockEntity otherBE = level.getBlockEntity(otherPosition);
            if (otherBE instanceof TrackBlockEntity tbe) {
                blockEntity.addConnection(bezierConnection);
                tbe.addConnection(bezierConnection.secondary());
            }
        }
    }

    public void captureSmoothingHandles() {
        boolean first = true;
        previousSmoothingHandles = Couple.create(null, null);
        for (BezierConnection bezierConnection : blockEntity.connections.values()) {
            previousSmoothingHandles.set(
                first,
                Pair.of(bezierConnection.starts.getFirst(), bezierConnection.smoothing == null ? 0 : bezierConnection.smoothing.getFirst())
            );
            first = false;
        }
    }

    public void undoSmoothing() {
        if (smoothingAngle.isEmpty())
            return;
        if (previousSmoothingHandles == null)
            return;
        if (blockEntity.connections.size() == 2)
            return;

        BlockState blockState = blockEntity.getCachedState();
        BlockPos worldPosition = blockEntity.getPos();
        World level = blockEntity.getWorld();

        List<BezierConnection> validConnections = new ArrayList<>();
        for (BezierConnection bezierConnection : blockEntity.connections.values()) {
            BlockPos otherPosition = bezierConnection.getKey();
            BlockEntity otherBE = level.getBlockEntity(otherPosition);
            if (otherBE instanceof TrackBlockEntity tbe && tbe.connections.containsKey(worldPosition))
                validConnections.add(bezierConnection);
        }

        blockEntity.removeInboundConnections(false);
        TrackPropagator.onRailRemoved(level, worldPosition, blockState);
        blockEntity.connections.clear();
        smoothingAngle = Optional.empty();

        for (BezierConnection bezierConnection : validConnections) {
            blockEntity.addConnection(restoreToOriginalCurve(bezierConnection));

            BlockPos otherPosition = bezierConnection.getKey();
            BlockState otherState = level.getBlockState(otherPosition);
            if (!(otherState.getBlock() instanceof TrackBlock))
                continue;
            level.setBlockState(otherPosition, otherState.with(TrackBlock.HAS_BE, true));
            BlockEntity otherBE = level.getBlockEntity(otherPosition);
            if (otherBE instanceof TrackBlockEntity tbe)
                tbe.addConnection(bezierConnection.secondary());
        }

        blockEntity.notifyUpdate();
        previousSmoothingHandles = null;
        TrackPropagator.onRailAdded(level, worldPosition, blockState);
    }

    public BezierConnection restoreToOriginalCurve(BezierConnection bezierConnection) {
        if (bezierConnection.smoothing != null) {
            bezierConnection.smoothing.setFirst(0);
            if (bezierConnection.smoothing.getFirst() == 0 && bezierConnection.smoothing.getSecond() == 0)
                bezierConnection.smoothing = null;
        }
        Vec3d raisedStart = bezierConnection.starts.getFirst();
        bezierConnection.starts.setFirst(new TrackNodeLocation(raisedStart).getLocation());
        bezierConnection.axes.setFirst(bezierConnection.axes.getFirst().multiply(1, 0, 1).normalize());
        return bezierConnection;
    }

    public int getYOffsetForAxisEnd(Vec3d end) {
        if (smoothingAngle.isEmpty())
            return 0;
        for (BezierConnection bezierConnection : blockEntity.connections.values())
            if (compareHandles(bezierConnection.starts.getFirst(), end))
                return bezierConnection.yOffsetAt(end);
        if (previousSmoothingHandles == null)
            return 0;
        for (Pair<Vec3d, Integer> handle : previousSmoothingHandles)
            if (handle != null && compareHandles(handle.getFirst(), end))
                return handle.getSecond();
        return 0;
    }

    public static boolean compareHandles(Vec3d handle1, Vec3d handle2) {
        return new TrackNodeLocation(handle1).getLocation().multiply(1, 0, 1)
            .squaredDistanceTo(new TrackNodeLocation(handle2).getLocation().multiply(1, 0, 1)) < 1 / 512f;
    }

}
