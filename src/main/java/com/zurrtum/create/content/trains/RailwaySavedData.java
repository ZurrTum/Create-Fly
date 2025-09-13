package com.zurrtum.create.content.trains;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.graph.DimensionPalette;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.graph.TrackGraph;
import com.zurrtum.create.content.trains.signal.SignalBoundary;
import com.zurrtum.create.content.trains.signal.SignalEdgeGroup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RailwaySavedData extends PersistentState {
    public static final Codec<RailwaySavedData> CODEC = Codec.of(RailwaySavedData::save, RailwaySavedData::load);
    private static final PersistentStateType<RailwaySavedData> TYPE = new PersistentStateType<>("create_tracks", RailwaySavedData::new, CODEC, null);

    private Map<UUID, TrackGraph> trackNetworks = new HashMap<>();
    private Map<UUID, SignalEdgeGroup> signalEdgeGroups = new HashMap<>();
    private Map<UUID, Train> trains = new HashMap<>();

    public static <T> DataResult<T> save(final RailwaySavedData input, final DynamicOps<T> ops, final T prefix) {
        T empty = ops.empty();
        RecordBuilder<T> builder = ops.mapBuilder();
        DimensionPalette dimensions = new DimensionPalette();
        ListBuilder<T> trackNetworks = ops.listBuilder();
        for (TrackGraph tg : input.trackNetworks.values()) {
            trackNetworks.add(TrackGraph.encode(tg, ops, empty, dimensions));
        }
        builder.add("RailGraphs", trackNetworks.build(empty));
        ListBuilder<T> signalEdgeGroups = ops.listBuilder();
        for (SignalEdgeGroup seg : input.signalEdgeGroups.values()) {
            if (seg.fallbackGroup && !input.trackNetworks.containsKey(seg.id)) {
                continue;
            }
            signalEdgeGroups.add(seg, SignalEdgeGroup.CODEC);
        }
        builder.add("SignalBlocks", signalEdgeGroups.build(empty));
        ListBuilder<T> trains = ops.listBuilder();
        for (Train train : input.trains.values()) {
            trains.add(Train.encode(train, ops, empty, dimensions));
        }
        builder.add("Trains", trains.build(empty));
        builder.add("DimensionPalette", dimensions, DimensionPalette.CODEC);
        return builder.build(prefix);
    }

    static <T> DataResult<Pair<RailwaySavedData, T>> load(final DynamicOps<T> ops, final T input) {
        RailwaySavedData sd = new RailwaySavedData();
        sd.trackNetworks = new HashMap<>();
        sd.signalEdgeGroups = new HashMap<>();
        sd.trains = new HashMap<>();
        MapLike<T> map = ops.getMap(input).getOrThrow();
        DimensionPalette dimensions = DimensionPalette.CODEC.decode(ops, map.get("DimensionPalette")).getOrThrow().getFirst();
        ops.getList(map.get("RailGraphs")).getOrThrow().accept(item -> {
            TrackGraph graph = TrackGraph.decode(ops, item, dimensions);
            sd.trackNetworks.put(graph.id, graph);
        });
        ops.getList(map.get("SignalBlocks")).getOrThrow().accept(item -> {
            SignalEdgeGroup group = SignalEdgeGroup.CODEC.decode(ops, item).getOrThrow().getFirst();
            sd.signalEdgeGroups.put(group.id, group);
        });
        ops.getList(map.get("Trains")).getOrThrow().accept(item -> {
            Train train = Train.decode(ops, item, sd.trackNetworks, dimensions);
            sd.trains.put(train.id, train);
        });

        for (TrackGraph graph : sd.trackNetworks.values()) {
            for (SignalBoundary signal : graph.getPoints(EdgePointType.SIGNAL)) {
                UUID groupId = signal.groups.getFirst();
                UUID otherGroupId = signal.groups.getSecond();
                if (groupId == null || otherGroupId == null)
                    continue;
                SignalEdgeGroup group = sd.signalEdgeGroups.get(groupId);
                SignalEdgeGroup otherGroup = sd.signalEdgeGroups.get(otherGroupId);
                if (group == null || otherGroup == null)
                    continue;
                group.putAdjacent(otherGroupId);
                otherGroup.putAdjacent(groupId);
            }
        }

        return DataResult.success(Pair.of(sd, ops.empty()));
    }

    public Map<UUID, TrackGraph> getTrackNetworks() {
        return trackNetworks;
    }

    public Map<UUID, Train> getTrains() {
        return trains;
    }

    public Map<UUID, SignalEdgeGroup> getSignalBlocks() {
        return signalEdgeGroups;
    }

    private RailwaySavedData() {
    }

    public static RailwaySavedData load(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE);
    }

}
