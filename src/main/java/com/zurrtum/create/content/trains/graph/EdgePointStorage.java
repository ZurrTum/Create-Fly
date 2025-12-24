package com.zurrtum.create.content.trains.graph;

import com.mojang.serialization.*;
import com.zurrtum.create.Create;
import com.zurrtum.create.content.trains.signal.TrackEdgePoint;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EdgePointStorage {

    private Map<EdgePointType<?>, Map<UUID, TrackEdgePoint>> pointsByType;

    public EdgePointStorage() {
        pointsByType = new HashMap<>();
    }

    public <T extends TrackEdgePoint> void put(EdgePointType<T> type, TrackEdgePoint point) {
        getMap(type).put(point.getId(), point);
    }

    @SuppressWarnings("unchecked")
    public <T extends TrackEdgePoint> T get(EdgePointType<T> type, UUID id) {
        return (T) getMap(type).get(id);
    }

    @SuppressWarnings("unchecked")
    public <T extends TrackEdgePoint> T remove(EdgePointType<T> type, UUID id) {
        return (T) getMap(type).remove(id);
    }

    @SuppressWarnings("unchecked")
    public <T extends TrackEdgePoint> Collection<T> values(EdgePointType<T> type) {
        return getMap(type).values().stream().map(e -> (T) e).toList();
    }

    public Map<UUID, TrackEdgePoint> getMap(EdgePointType<? extends TrackEdgePoint> type) {
        return pointsByType.computeIfAbsent(type, t -> new HashMap<>());
    }

    public void tick(MinecraftServer server, TrackGraph graph, boolean preTrains) {
        for (Map<UUID, TrackEdgePoint> map : pointsByType.values()) {
            for (TrackEdgePoint point : map.values()) {
                point.tick(server, graph, preTrains);
            }
        }
    }

    public void transferAll(TrackGraph target, EdgePointStorage other) {
        pointsByType.forEach((type, map) -> {
            other.getMap(type).putAll(map);
            map.values().forEach(ep -> Create.RAILWAYS.sync.pointAdded(target, ep));
        });
        pointsByType.clear();
    }

    public void write(ValueOutput view, DimensionPalette dimensions) {
        for (Map.Entry<EdgePointType<?>, Map<UUID, TrackEdgePoint>> entry : pointsByType.entrySet()) {
            EdgePointType<?> type = entry.getKey();
            ValueOutput.ValueOutputList list = view.childrenList(type.getId().toString());
            entry.getValue().values().forEach(edgePoint -> edgePoint.write(list.addChild(), dimensions));
        }
    }

    public static <T> DataResult<T> encode(final EdgePointStorage input, final DynamicOps<T> ops, final T empty, DimensionPalette dimensions) {
        RecordBuilder<T> map = ops.mapBuilder();
        for (Map.Entry<EdgePointType<?>, Map<UUID, TrackEdgePoint>> entry : input.pointsByType.entrySet()) {
            EdgePointType<?> type = entry.getKey();
            ListBuilder<T> list = ops.listBuilder();
            for (TrackEdgePoint edgePoint : entry.getValue().values()) {
                list.add(edgePoint.encode(ops, empty, dimensions));
            }
            map.add(type.getId().toString(), list.build(empty));
        }
        return map.build(empty);
    }

    public void read(ValueInput view, DimensionPalette dimensions) {
        for (EdgePointType<?> type : EdgePointType.TYPES.values()) {
            Map<UUID, TrackEdgePoint> map = getMap(type);
            view.childrenListOrEmpty(type.getId().toString()).forEach(item -> {
                TrackEdgePoint edgePoint = type.create();
                edgePoint.read(item, false, dimensions);
                map.put(edgePoint.getId(), edgePoint);
            });
        }
    }

    public <T> void decode(final DynamicOps<T> ops, final T input, DimensionPalette dimensions) {
        MapLike<T> map = ops.getMap(input).getOrThrow();
        for (EdgePointType<?> type : EdgePointType.TYPES.values()) {
            Map<UUID, TrackEdgePoint> typeMap = getMap(type);
            ops.getList(map.get(type.getId().toString())).ifSuccess(list -> list.accept(item -> {
                TrackEdgePoint edgePoint = type.create();
                edgePoint.decode(ops, item, false, dimensions);
                typeMap.put(edgePoint.getId(), edgePoint);
            }));
        }
    }

}
