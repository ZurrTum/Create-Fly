package com.zurrtum.create.content.trains.observer;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.zurrtum.create.Create;
import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.graph.DimensionPalette;
import com.zurrtum.create.content.trains.graph.TrackEdge;
import com.zurrtum.create.content.trains.graph.TrackGraph;
import com.zurrtum.create.content.trains.signal.SignalPropagator;
import com.zurrtum.create.content.trains.signal.SingleBlockEntityEdgePoint;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
import net.minecraft.world.World;

import java.util.UUID;

public class TrackObserver extends SingleBlockEntityEdgePoint {

    private int activated;
    private FilterItemStack filter;
    private UUID currentTrain;

    public TrackObserver() {
        activated = 0;
        filter = FilterItemStack.empty();
        currentTrain = null;
    }

    @Override
    public void blockEntityAdded(BlockEntity blockEntity, boolean front) {
        super.blockEntityAdded(blockEntity, front);
        ServerFilteringBehaviour filteringBehaviour = BlockEntityBehaviour.get(blockEntity, ServerFilteringBehaviour.TYPE);
        if (filteringBehaviour != null)
            setFilterAndNotify(blockEntity.getWorld(), filteringBehaviour.getFilter());
    }

    @Override
    public void tick(MinecraftServer server, TrackGraph graph, boolean preTrains) {
        super.tick(server, graph, preTrains);
        if (isActivated())
            activated--;
        if (!isActivated())
            currentTrain = null;
    }

    public void setFilterAndNotify(World level, ItemStack filter) {
        this.filter = FilterItemStack.of(filter.copy());
        notifyTrains(level);
    }

    private void notifyTrains(World level) {
        TrackGraph graph = Create.RAILWAYS.sided(level).getGraph(edgeLocation.getFirst());
        if (graph == null)
            return;
        TrackEdge edge = graph.getConnection(edgeLocation.map(graph::locateNode));
        if (edge == null)
            return;
        SignalPropagator.notifyTrains(graph, edge);
    }

    public FilterItemStack getFilter() {
        return filter;
    }

    public UUID getCurrentTrain() {
        return currentTrain;
    }

    public boolean isActivated() {
        return activated > 0;
    }

    public void keepAlive(Train train) {
        activated = 8;
        currentTrain = train.id;
    }

    @Override
    public void read(ReadView view, boolean migration, DimensionPalette dimensions) {
        super.read(view, migration, dimensions);
        activated = view.getInt("Activated", 0);
        filter = view.read("Filter", FilterItemStack.CODEC).orElseGet(FilterItemStack::empty);
        currentTrain = view.read("TrainId", Uuids.INT_STREAM_CODEC).orElse(null);
    }

    @Override
    public <T> void decode(DynamicOps<T> ops, T input, boolean migration, DimensionPalette dimensions) {
        super.decode(ops, input, migration, dimensions);
        MapLike<T> map = ops.getMap(input).getOrThrow();
        activated = ops.getNumberValue(map.get("Activated")).getOrThrow().intValue();
        filter = FilterItemStack.CODEC.parse(ops, map.get("Filter")).result().orElseGet(FilterItemStack::empty);
        currentTrain = Uuids.INT_STREAM_CODEC.parse(ops, map.get("TrainId")).result().orElse(null);
    }

    @Override
    public void write(WriteView view, DimensionPalette dimensions) {
        super.write(view, dimensions);
        view.putInt("Activated", activated);
        view.put("Filter", FilterItemStack.CODEC, filter);
        if (currentTrain != null)
            view.put("TrainId", Uuids.INT_STREAM_CODEC, currentTrain);
    }

    @Override
    public <T> DataResult<T> encode(DynamicOps<T> ops, T empty, DimensionPalette dimensions) {
        DataResult<T> prefix = super.encode(ops, empty, dimensions);
        RecordBuilder<T> map = ops.mapBuilder();
        map.add("Activated", ops.createInt(activated));
        map.add("Filter", filter, FilterItemStack.CODEC);
        if (currentTrain != null)
            map.add("TrainId", currentTrain, Uuids.INT_STREAM_CODEC);
        return map.build(prefix);
    }
}
