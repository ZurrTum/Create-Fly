package com.zurrtum.create.content.trains.graph;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.zurrtum.create.catnip.data.Couple;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;

import java.util.Iterator;
import java.util.UUID;

public class TrackEdgeIntersection {

    public double location;
    public Couple<TrackNodeLocation> target;
    public double targetLocation;
    public UUID groupId;
    public UUID id;

    public TrackEdgeIntersection() {
        id = UUID.randomUUID();
    }

    public boolean isNear(double location) {
        return Math.abs(location - this.location) < 1 / 32f;
    }

    public boolean targets(TrackNodeLocation target1, TrackNodeLocation target2) {
        return target1.equals(target.getFirst()) && target2.equals(target.getSecond()) || target1.equals(target.getSecond()) && target2.equals(target.getFirst());
    }

    public void write(WriteView view, DimensionPalette dimensions) {
        view.put("Id", Uuids.INT_STREAM_CODEC, id);
        if (groupId != null)
            view.put("GroupId", Uuids.INT_STREAM_CODEC, groupId);
        view.putDouble("Location", location);
        view.putDouble("TargetLocation", targetLocation);
        WriteView.ListView edge = view.getList("TargetEdge");
        target.getFirst().write(edge.add(), dimensions);
        target.getSecond().write(edge.add(), dimensions);
    }

    public static <T> DataResult<T> encode(final TrackEdgeIntersection input, final DynamicOps<T> ops, final T empty, DimensionPalette dimensions) {
        RecordBuilder<T> builder = ops.mapBuilder();
        builder.add("Id", input.id, Uuids.INT_STREAM_CODEC);
        if (input.groupId != null)
            builder.add("GroupId", input.groupId, Uuids.INT_STREAM_CODEC);
        builder.add("Location", ops.createDouble(input.location));
        builder.add("TargetLocation", ops.createDouble(input.targetLocation));
        ListBuilder<T> edge = ops.listBuilder();
        edge.add(TrackNodeLocation.encode(input.target.getFirst(), ops, empty, dimensions));
        edge.add(TrackNodeLocation.encode(input.target.getSecond(), ops, empty, dimensions));
        builder.add("TargetEdge", edge.build(empty));
        return builder.build(empty);
    }

    public static TrackEdgeIntersection read(ReadView view, DimensionPalette dimensions) {
        TrackEdgeIntersection intersection = new TrackEdgeIntersection();
        intersection.id = view.read("Id", Uuids.INT_STREAM_CODEC).orElseThrow();
        view.read("GroupId", Uuids.INT_STREAM_CODEC).ifPresent(id -> intersection.groupId = id);
        intersection.location = view.getDouble("Location", 0);
        intersection.targetLocation = view.getDouble("TargetLocation", 0);
        Iterator<ReadView> edge = view.getListReadView("TargetEdge").iterator();
        intersection.target = Couple.create(TrackNodeLocation.read(edge.next(), dimensions), TrackNodeLocation.read(edge.next(), dimensions));
        return intersection;
    }

    public static <T> TrackEdgeIntersection decode(DynamicOps<T> ops, T input, DimensionPalette dimensions) {
        MapLike<T> map = ops.getMap(input).getOrThrow();
        TrackEdgeIntersection intersection = new TrackEdgeIntersection();
        intersection.id = Uuids.INT_STREAM_CODEC.decode(ops, map.get("Id")).getOrThrow().getFirst();
        Uuids.INT_STREAM_CODEC.decode(ops, map.get("GroupId")).result().map(Pair::getFirst).ifPresent(id -> intersection.groupId = id);
        intersection.location = ops.getNumberValue(map.get("Location"), 0).intValue();
        intersection.targetLocation = ops.getNumberValue(map.get("TargetLocation"), 0).intValue();
        intersection.target = Couple.create(null, null);
        ops.getList(map.get("TargetEdge")).getOrThrow().accept(item -> {
            TrackNodeLocation location = TrackNodeLocation.decode(ops, item, dimensions);
            if (intersection.target.getFirst() == null) {
                intersection.target.setFirst(location);
            } else {
                intersection.target.setSecond(location);
            }
        });
        return intersection;
    }

}