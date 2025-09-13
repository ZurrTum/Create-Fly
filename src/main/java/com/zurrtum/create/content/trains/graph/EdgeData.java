package com.zurrtum.create.content.trains.graph;

import com.google.common.base.Objects;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.trains.signal.SignalBoundary;
import com.zurrtum.create.content.trains.signal.SignalEdgeGroup;
import com.zurrtum.create.content.trains.signal.TrackEdgePoint;
import net.minecraft.server.MinecraftServer;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EdgeData {

    public static final UUID passiveGroup = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private UUID singleSignalGroup;
    private List<TrackEdgePoint> points;
    private List<TrackEdgeIntersection> intersections;
    private TrackEdge edge;

    public EdgeData(TrackEdge edge) {
        this.edge = edge;
        points = new ArrayList<>();
        intersections = new ArrayList<>();
        singleSignalGroup = passiveGroup;
    }

    public boolean hasSignalBoundaries() {
        return singleSignalGroup == null;
    }

    public UUID getSingleSignalGroup() {
        return singleSignalGroup;
    }

    public void setSingleSignalGroup(@Nullable MinecraftServer server, @Nullable TrackGraph graph, UUID singleSignalGroup) {
        if (graph != null && !Objects.equal(singleSignalGroup, this.singleSignalGroup))
            refreshIntersectingSignalGroups(server, graph);
        this.singleSignalGroup = singleSignalGroup;
    }

    public void refreshIntersectingSignalGroups(MinecraftServer server, TrackGraph graph) {
        Map<UUID, SignalEdgeGroup> groups = Create.RAILWAYS.signalEdgeGroups;
        for (TrackEdgeIntersection intersection : intersections) {
            if (intersection.groupId == null)
                continue;
            SignalEdgeGroup group = groups.get(intersection.groupId);
            if (group != null)
                group.removeIntersection(server, intersection.id);
        }
        if (hasIntersections())
            graph.deferIntersectionUpdate(edge);
    }

    public boolean hasPoints() {
        return !points.isEmpty();
    }

    public boolean hasIntersections() {
        return !intersections.isEmpty();
    }

    public List<TrackEdgeIntersection> getIntersections() {
        return intersections;
    }

    public void addIntersection(TrackGraph graph, UUID id, double position, TrackNode target1, TrackNode target2, double targetPosition) {
        TrackNodeLocation loc1 = target1.getLocation();
        TrackNodeLocation loc2 = target2.getLocation();

        for (TrackEdgeIntersection existing : intersections)
            if (existing.isNear(position) && existing.targets(loc1, loc2))
                return;

        TrackEdgeIntersection intersection = new TrackEdgeIntersection();
        intersection.id = id;
        intersection.location = position;
        intersection.target = Couple.create(loc1, loc2);
        intersection.targetLocation = targetPosition;
        intersections.add(intersection);
        graph.deferIntersectionUpdate(edge);
    }

    public void removeIntersection(MinecraftServer server, TrackGraph graph, UUID id) {
        refreshIntersectingSignalGroups(server, graph);
        for (Iterator<TrackEdgeIntersection> iterator = intersections.iterator(); iterator.hasNext(); ) {
            TrackEdgeIntersection existing = iterator.next();
            if (existing.id.equals(id))
                iterator.remove();
        }
    }

    public UUID getGroupAtPosition(TrackGraph graph, double position) {
        if (!hasSignalBoundaries())
            return getEffectiveEdgeGroupId(graph);
        SignalBoundary firstSignal = next(EdgePointType.SIGNAL, 0);
        if (firstSignal == null)
            return null;
        UUID currentGroup = firstSignal.getGroup(edge.node1);

        for (TrackEdgePoint trackEdgePoint : getPoints()) {
            if (!(trackEdgePoint instanceof SignalBoundary sb))
                continue;
            if (sb.getLocationOn(edge) >= position)
                return currentGroup;
            currentGroup = sb.getGroup(edge.node2);
        }

        return currentGroup;
    }

    public List<TrackEdgePoint> getPoints() {
        return points;
    }

    public UUID getEffectiveEdgeGroupId(TrackGraph graph) {
        return singleSignalGroup == null ? null : singleSignalGroup.equals(passiveGroup) ? graph.id : singleSignalGroup;
    }

    public void removePoint(MinecraftServer server, TrackGraph graph, TrackEdgePoint point) {
        points.remove(point);
        if (point.getType() == EdgePointType.SIGNAL) {
            boolean noSignalsRemaining = next(point.getType(), 0) == null;
            setSingleSignalGroup(server, graph, noSignalsRemaining ? passiveGroup : null);
        }
    }

    public <T extends TrackEdgePoint> void addPoint(MinecraftServer server, TrackGraph graph, TrackEdgePoint point) {
        if (point.getType() == EdgePointType.SIGNAL)
            setSingleSignalGroup(server, graph, null);
        double locationOn = point.getLocationOn(edge);
        int i = 0;
        for (; i < points.size(); i++)
            if (points.get(i).getLocationOn(edge) > locationOn)
                break;
        points.add(i, point);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends TrackEdgePoint> T next(EdgePointType<T> type, double minPosition) {
        for (TrackEdgePoint point : points)
            if (point.getType() == type && point.getLocationOn(edge) > minPosition)
                return (T) point;
        return null;
    }

    @Nullable
    public TrackEdgePoint next(double minPosition) {
        for (TrackEdgePoint point : points)
            if (point.getLocationOn(edge) > minPosition)
                return point;
        return null;
    }

    @Nullable
    public <T extends TrackEdgePoint> T get(EdgePointType<T> type, double exactPosition) {
        T next = next(type, exactPosition - .5f);
        if (next != null && MathHelper.approximatelyEquals(next.getLocationOn(edge), exactPosition))
            return next;
        return null;
    }

    public void write(WriteView view, DimensionPalette dimensions) {
        if (singleSignalGroup == passiveGroup)
            view.putBoolean("PassiveGroup", true);
        else if (singleSignalGroup != null)
            view.put("SignalGroup", Uuids.INT_STREAM_CODEC, singleSignalGroup);

        if (hasPoints()) {
            WriteView.ListView list = view.getList("Points");
            for (TrackEdgePoint point : points) {
                WriteView item = list.add();
                item.put("Id", Uuids.INT_STREAM_CODEC, point.id);
                item.put("Type", EdgePointType.CODEC, point.getType());
            }
        }
        if (hasIntersections()) {
            WriteView.ListView list = view.getList("Intersections");
            for (TrackEdgeIntersection intersection : intersections) {
                intersection.write(list.add(), dimensions);
            }
        }
    }

    public static <T> DataResult<T> encode(final EdgeData input, final DynamicOps<T> ops, final T empty, DimensionPalette dimensions) {
        RecordBuilder<T> builder = ops.mapBuilder();
        if (input.singleSignalGroup == passiveGroup)
            builder.add("PassiveGroup", ops.createBoolean(true));
        else if (input.singleSignalGroup != null)
            builder.add("SignalGroup", input.singleSignalGroup, Uuids.INT_STREAM_CODEC);

        if (input.hasPoints()) {
            ListBuilder<T> list = ops.listBuilder();
            for (TrackEdgePoint point : input.points) {
                RecordBuilder<T> item = ops.mapBuilder();
                item.add("Id", point.id, Uuids.INT_STREAM_CODEC);
                item.add("Type", point.getType(), EdgePointType.CODEC);
                list.add(item.build(empty));
            }
            builder.add("Points", list.build(empty));
        }
        if (input.hasIntersections()) {
            ListBuilder<T> list = ops.listBuilder();
            for (TrackEdgeIntersection intersection : input.intersections) {
                list.add(TrackEdgeIntersection.encode(intersection, ops, empty, dimensions));
            }
            builder.add("Intersections", list.build(empty));
        }
        return builder.build(empty);
    }

    public static EdgeData read(ReadView view, TrackEdge edge, TrackGraph graph, DimensionPalette dimensions) {
        EdgeData data = new EdgeData(edge);
        view.read("SignalGroup", Uuids.INT_STREAM_CODEC).ifPresentOrElse(
            id -> data.singleSignalGroup = id, () -> {
                if (!view.getBoolean("PassiveGroup", false)) {
                    data.singleSignalGroup = null;
                }
            }
        );

        view.getOptionalListReadView("Points").ifPresent(list -> list.forEach(item -> item.read("Type", EdgePointType.CODEC)
            .flatMap(type -> item.read("Id", Uuids.INT_STREAM_CODEC).map(id -> graph.getPoint(type, id))).ifPresent(data.points::add)));
        view.getOptionalListReadView("Intersections")
            .ifPresent(list -> list.forEach(item -> data.intersections.add(TrackEdgeIntersection.read(item, dimensions))));
        return data;
    }

    public static <T> EdgeData decode(DynamicOps<T> ops, T input, TrackEdge edge, TrackGraph graph, DimensionPalette dimensions) {
        EdgeData data = new EdgeData(edge);
        MapLike<T> map = ops.getMap(input).getOrThrow();
        Uuids.INT_STREAM_CODEC.decode(ops, map.get("SignalGroup")).result().map(Pair::getFirst).ifPresentOrElse(
            id -> data.singleSignalGroup = id, () -> {
                if (!Optional.ofNullable(map.get("PassiveGroup")).flatMap(value -> ops.getBooleanValue(value).result()).orElse(false)) {
                    data.singleSignalGroup = null;
                }
            }
        );
        ops.getList(map.get("Points")).ifSuccess(list -> list.accept(item -> {
            MapLike<T> point = ops.getMap(item).getOrThrow();
            EdgePointType.CODEC.decode(ops, point.get("Type")).result().map(Pair::getFirst)
                .flatMap(type -> Uuids.INT_STREAM_CODEC.decode(ops, point.get("Id")).result().map(Pair::getFirst).map(id -> graph.getPoint(type, id)))
                .ifPresent(data.points::add);
        }));
        ops.getList(map.get("Intersections")).ifSuccess(list -> list.accept(item -> {
            data.intersections.add(TrackEdgeIntersection.decode(ops, item, dimensions));
        }));
        return data;
    }

}
