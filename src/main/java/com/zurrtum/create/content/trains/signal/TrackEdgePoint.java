package com.zurrtum.create.content.trains.signal;

import com.mojang.serialization.*;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.trains.graph.*;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.Iterator;
import java.util.UUID;

public abstract class TrackEdgePoint {

    public UUID id;
    public Couple<TrackNodeLocation> edgeLocation;
    public double position;
    private EdgePointType<?> type;

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setType(EdgePointType<?> type) {
        this.type = type;
    }

    public EdgePointType<?> getType() {
        return type;
    }

    public abstract boolean canMerge();

    public boolean canCoexistWith(EdgePointType<?> otherType, boolean front) {
        return false;
    }

    public abstract void invalidate(LevelAccessor level);

    protected void invalidateAt(LevelAccessor level, BlockPos blockEntityPos) {
        TrackTargetingBehaviour<?> behaviour = BlockEntityBehaviour.get(level, blockEntityPos, TrackTargetingBehaviour.TYPE);
        if (behaviour == null)
            return;
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(() -> "TrackEdgePoint", Create.LOGGER)) {
            TagValueOutput view = TagValueOutput.createWithContext(logging, level.registryAccess());
            DimensionPalette dimensions = new DimensionPalette();
            write(view, dimensions);
            view.store("DimensionPalette", DimensionPalette.CODEC, dimensions);
            behaviour.invalidateEdgePoint(view.buildResult());
        }
    }

    public abstract void blockEntityAdded(BlockEntity blockEntity, boolean front);

    public abstract void blockEntityRemoved(MinecraftServer server, BlockPos blockEntityPos, boolean front);

    public void onRemoved(MinecraftServer server, TrackGraph graph) {
    }

    public void setLocation(Couple<TrackNodeLocation> nodes, double position) {
        this.edgeLocation = nodes;
        this.position = position;
    }

    public double getLocationOn(TrackEdge edge) {
        return isPrimary(edge.node1) ? edge.getLength() - position : position;
    }

    public boolean canNavigateVia(TrackNode side) {
        return true;
    }

    public boolean isPrimary(TrackNode node1) {
        return edgeLocation.getSecond().equals(node1.getLocation());
    }

    public void read(ValueInput view, boolean migration, DimensionPalette dimensions) {
        if (migration)
            return;

        id = view.read("Id", UUIDUtil.CODEC).orElseThrow();
        position = view.getDoubleOr("Position", 0);
        Iterator<ValueInput> edge = view.childrenListOrEmpty("Edge").iterator();
        edgeLocation = Couple.create(TrackNodeLocation.read(edge.next(), dimensions), TrackNodeLocation.read(edge.next(), dimensions));
    }

    public <T> void decode(final DynamicOps<T> ops, T input, boolean migration, DimensionPalette dimensions) {
        if (migration)
            return;

        MapLike<T> map = ops.getMap(input).getOrThrow();
        id = UUIDUtil.CODEC.decode(ops, map.get("Id")).getOrThrow().getFirst();
        position = ops.getNumberValue(map.get("Position"), 0).doubleValue();
        edgeLocation = Couple.create(null, null);
        ops.getList(map.get("Edge")).getOrThrow().accept(item -> {
            TrackNodeLocation location = TrackNodeLocation.decode(ops, item, dimensions);
            if (edgeLocation.getFirst() == null) {
                edgeLocation.setFirst(location);
            } else {
                edgeLocation.setSecond(location);
            }
        });
    }

    public void read(FriendlyByteBuf buffer, DimensionPalette dimensions) {
        id = buffer.readUUID();
        edgeLocation = Couple.create(() -> TrackNodeLocation.receive(buffer, dimensions));
        position = buffer.readDouble();
    }

    public void write(ValueOutput view, DimensionPalette dimensions) {
        view.store("Id", UUIDUtil.CODEC, id);
        view.putDouble("Position", position);
        ValueOutput.ValueOutputList edge = view.childrenList("Edge");
        edgeLocation.getFirst().write(edge.addChild(), dimensions);
        edgeLocation.getSecond().write(edge.addChild(), dimensions);
    }

    public <T> DataResult<T> encode(final DynamicOps<T> ops, final T empty, DimensionPalette dimensions) {
        RecordBuilder<T> map = ops.mapBuilder();
        map.add("Id", id, UUIDUtil.CODEC);
        map.add("Position", ops.createDouble(position));
        ListBuilder<T> edge = ops.listBuilder();
        edge.add(TrackNodeLocation.encode(edgeLocation.getFirst(), ops, empty, dimensions));
        edge.add(TrackNodeLocation.encode(edgeLocation.getSecond(), ops, empty, dimensions));
        map.add("Edge", edge.build(empty));
        return map.build(empty);
    }

    public void write(FriendlyByteBuf buffer, DimensionPalette dimensions) {
        buffer.writeResourceLocation(type.getId());
        buffer.writeUUID(id);
        edgeLocation.forEach(loc -> loc.send(buffer, dimensions));
        buffer.writeDouble(position);
    }

    public void tick(MinecraftServer server, TrackGraph graph, boolean preTrains) {
    }

    protected void removeFromAllGraphs(MinecraftServer server) {
        for (TrackGraph trackGraph : Create.RAILWAYS.trackNetworks.values())
            if (trackGraph.removePoint(server, getType(), id) != null)
                return;
    }

}
