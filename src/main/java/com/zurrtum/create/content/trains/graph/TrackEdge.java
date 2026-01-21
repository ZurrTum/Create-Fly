package com.zurrtum.create.content.trains.graph;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class TrackEdge {

    public TrackNode node1;
    public TrackNode node2;
    @Nullable BezierConnection turn;
    public EdgeData edgeData;
    boolean interDimensional;
    TrackMaterial trackMaterial;

    public TrackEdge(TrackNode node1, TrackNode node2, @Nullable BezierConnection turn, TrackMaterial trackMaterial) {
        this.interDimensional = !node1.location.dimension.equals(node2.location.dimension);
        this.edgeData = new EdgeData(this);
        this.node1 = node1;
        this.node2 = node2;
        this.turn = turn;
        this.trackMaterial = trackMaterial;
    }

    public TrackMaterial getTrackMaterial() {
        return trackMaterial;
    }

    public boolean isTurn() {
        return turn != null;
    }

    public boolean isInterDimensional() {
        return interDimensional;
    }

    public EdgeData getEdgeData() {
        return edgeData;
    }

    @Nullable
    public BezierConnection getTurn() {
        return turn;
    }

    public Vec3 getDirection(boolean fromFirst) {
        return getPosition(null, fromFirst ? 0.25f : 1).subtract(getPosition(null, fromFirst ? 0 : 0.75f)).normalize();
    }

    public Vec3 getDirectionAt(double t) {
        double length = getLength();
        double step = .5f / length;
        t /= length;
        Vec3 ahead = getPosition(null, Math.min(1, t + step));
        Vec3 behind = getPosition(null, Math.max(0, t - step));
        return ahead.subtract(behind).normalize();
    }

    public boolean canTravelTo(TrackEdge other) {
        if (isInterDimensional() || other.isInterDimensional())
            return true;
        Vec3 newDirection = other.getDirection(true);
        return getDirection(false).dot(newDirection) > 7 / 8f;
    }

    public double getLength() {
        return isInterDimensional() ? 0 : isTurn() ? turn.getLength() : node1.location.getLocation().distanceTo(node2.location.getLocation());
    }

    public double incrementT(double currentT, double distance) {
        boolean tooFar = Math.abs(distance) > 5;
        double length = getLength();
        distance = distance / (length == 0 ? 1 : length);
        return !tooFar && isTurn() ? turn.incrementT(currentT, distance) : currentT + distance;
    }

    public Vec3 getPosition(@Nullable TrackGraph trackGraph, double t) {
        if (isTurn())
            return turn.getPosition(Mth.clamp(t, 0, 1));
        if (trackGraph != null && (node1.location.yOffsetPixels != 0 || node2.location.yOffsetPixels != 0)) {
            Vec3 positionSmoothed = getPositionSmoothed(trackGraph, t);
            if (positionSmoothed != null)
                return positionSmoothed;
        }
        return VecHelper.lerp((float) t, node1.location.getLocation(), node2.location.getLocation());
    }

    public Vec3 getNormal(@Nullable TrackGraph trackGraph, double t) {
        if (isTurn())
            return turn.getNormal(Mth.clamp(t, 0, 1));
        if (trackGraph != null && (node1.location.yOffsetPixels != 0 || node2.location.yOffsetPixels != 0)) {
            Vec3 normalSmoothed = getNormalSmoothed(trackGraph, t);
            if (normalSmoothed != null)
                return normalSmoothed;
        }
        return node1.getNormal();
    }

    @Nullable
    public Vec3 getPositionSmoothed(TrackGraph trackGraph, double t) {
        Vec3 node1Location = null;
        Vec3 node2Location = null;
        for (TrackEdge trackEdge : trackGraph.getConnectionsFrom(node1).values())
            if (trackEdge.isTurn())
                node1Location = trackEdge.getPosition(trackGraph, 0);
        for (TrackEdge trackEdge : trackGraph.getConnectionsFrom(node2).values())
            if (trackEdge.isTurn())
                node2Location = trackEdge.getPosition(trackGraph, 0);
        if (node1Location == null || node2Location == null)
            return null;
        return VecHelper.lerp((float) t, node1Location, node2Location);
    }

    @Nullable
    public Vec3 getNormalSmoothed(TrackGraph trackGraph, double t) {
        Vec3 node1Normal = null;
        Vec3 node2Normal = null;
        for (TrackEdge trackEdge : trackGraph.getConnectionsFrom(node1).values())
            if (trackEdge.isTurn())
                node1Normal = trackEdge.getNormal(trackGraph, 0);
        for (TrackEdge trackEdge : trackGraph.getConnectionsFrom(node2).values())
            if (trackEdge.isTurn())
                node2Normal = trackEdge.getNormal(trackGraph, 0);
        if (node1Normal == null || node2Normal == null)
            return null;
        return VecHelper.lerp(0.5f, node1Normal, node2Normal);
    }

    public Collection<double[]> getIntersection(TrackNode node1, TrackNode node2, TrackEdge other, TrackNode other1, TrackNode other2) {
        Vec3 v1 = node1.location.getLocation();
        Vec3 v2 = node2.location.getLocation();
        Vec3 w1 = other1.location.getLocation();
        Vec3 w2 = other2.location.getLocation();

        if (isInterDimensional() || other.isInterDimensional())
            return Collections.emptyList();
        if (v1.y != v2.y || v1.y != w1.y || v1.y != w2.y)
            return Collections.emptyList();

        if (!isTurn()) {
            if (!other.isTurn())
                return ImmutableList.of(VecHelper.intersectRanged(v1, w1, v2, w2, Axis.Y));
            return other.getIntersection(other1, other2, this, node1, node2).stream().map(a -> new double[]{a[1], a[0]}).toList();
        }

        AABB bb = turn.getBounds();

        if (!other.isTurn()) {
            if (!bb.intersects(w1, w2))
                return Collections.emptyList();

            Vec3 seg1 = v1;
            Vec3 seg2 = null;
            double t = 0;

            Collection<double[]> intersections = new ArrayList<>();
            for (int i = 0; i < turn.getSegmentCount(); i++) {
                double tOffset = t;
                t += .5;
                seg2 = getPosition(null, t / getLength());
                double[] intersection = VecHelper.intersectRanged(seg1, w1, seg2, w2, Axis.Y);
                seg1 = seg2;
                if (intersection == null)
                    continue;
                intersection[0] += tOffset;
                intersections.add(intersection);
            }

            return intersections;
        }

        if (!bb.intersects(other.turn.getBounds()))
            return Collections.emptyList();

        Vec3 seg1 = v1;
        Vec3 seg2 = null;
        double t = 0;

        Collection<double[]> intersections = new ArrayList<>();
        for (int i = 0; i < turn.getSegmentCount(); i++) {
            double tOffset = t;
            t += .5;
            seg2 = getPosition(null, t / getLength());

            Vec3 otherSeg1 = w1;
            Vec3 otherSeg2 = null;
            double u = 0;

            for (int j = 0; j < other.turn.getSegmentCount(); j++) {
                double uOffset = u;
                u += .5;
                otherSeg2 = other.getPosition(null, u / other.getLength());

                double[] intersection = VecHelper.intersectRanged(seg1, otherSeg1, seg2, otherSeg2, Axis.Y);
                otherSeg1 = otherSeg2;

                if (intersection == null)
                    continue;

                intersection[0] += tOffset;
                intersection[1] += uOffset;
                intersections.add(intersection);
            }

            seg1 = seg2;
        }

        return intersections;
    }

    public void write(ValueOutput view, DimensionPalette dimensions) {
        if (isTurn()) {
            view.store("BezierConnection", BezierConnection.CODEC, turn);
        }
        edgeData.write(view.child("Signals"), dimensions);
        view.store("Material", TrackMaterial.CODEC, getTrackMaterial());
    }

    public static <T> DataResult<T> encode(final TrackEdge input, final DynamicOps<T> ops, final T empty, DimensionPalette dimensions) {
        RecordBuilder<T> builder = ops.mapBuilder();
        if (input.isTurn()) {
            builder.add("BezierConnection", input.turn, BezierConnection.CODEC);
        }
        builder.add("Signals", EdgeData.encode(input.edgeData, ops, empty, dimensions));
        builder.add("Material", input.getTrackMaterial(), TrackMaterial.CODEC);
        return builder.build(empty);
    }

    public static TrackEdge read(TrackNode node1, TrackNode node2, ValueInput view, TrackGraph graph, DimensionPalette dimensions) {
        TrackEdge trackEdge = new TrackEdge(
            node1,
            node2,
            view.read("BezierConnection", BezierConnection.CODEC).orElse(null),
            view.read("Material", TrackMaterial.CODEC).orElseThrow()
        );
        trackEdge.edgeData = EdgeData.read(view.childOrEmpty("Signals"), trackEdge, graph, dimensions);
        return trackEdge;
    }

    public static <T> TrackEdge decode(
        TrackNode node1,
        TrackNode node2,
        final DynamicOps<T> ops,
        final T input,
        TrackGraph graph,
        DimensionPalette dimensions
    ) {
        MapLike<T> map = ops.getMap(input).getOrThrow();
        TrackEdge trackEdge = new TrackEdge(
            node1,
            node2,
            BezierConnection.CODEC.decode(ops, map.get("BezierConnection")).result().map(Pair::getFirst).orElse(null),
            TrackMaterial.CODEC.decode(ops, map.get("Material")).getOrThrow().getFirst()
        );
        trackEdge.edgeData = EdgeData.decode(ops, map.get("Signals"), trackEdge, graph, dimensions);
        return trackEdge;
    }

}
