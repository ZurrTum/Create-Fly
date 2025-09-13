package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.trains.graph.*;
import com.zurrtum.create.content.trains.signal.TrackEdgePoint;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.*;

public class TrackGraphSyncPacket extends TrackGraphPacket implements S2CPacket {
    public static final PacketCodec<PacketByteBuf, TrackGraphSyncPacket> CODEC = PacketCodec.of(
        TrackGraphSyncPacket::write,
        TrackGraphSyncPacket::new
    );

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, TrackGraphSyncPacket> callback() {
        return AllClientHandle::onTrackGraphSync;
    }

    @Override
    public PacketType<TrackGraphSyncPacket> getPacketType() {
        return AllPackets.SYNC_RAIL_GRAPH;
    }

    public static final int NULL_GROUP = 0, PASSIVE_GROUP = 1, GROUP = 2;

    public Map<Integer, Pair<TrackNodeLocation, Vec3d>> addedNodes;
    public List<Pair<Pair<Couple<Integer>, TrackMaterial>, BezierConnection>> addedEdges;
    public List<Integer> removedNodes;
    public List<TrackEdgePoint> addedEdgePoints;
    public List<UUID> removedEdgePoints;
    public Map<Integer, Pair<Integer, UUID>> splitSubGraphs;
    public Map<Couple<Integer>, Pair<Integer, List<UUID>>> updatedEdgeData;
    public boolean fullWipe;

    public TrackGraphSyncPacket(UUID graphId, int netId) {
        this.graphId = graphId;
        this.netId = netId;
        addedNodes = new HashMap<>();
        addedEdges = new ArrayList<>();
        removedNodes = new ArrayList<>();
        addedEdgePoints = new ArrayList<>();
        removedEdgePoints = new ArrayList<>();
        updatedEdgeData = new HashMap<>();
        splitSubGraphs = new HashMap<>();
        packetDeletesGraph = false;
    }

    public TrackGraphSyncPacket(PacketByteBuf buffer) {
        int size;

        graphId = buffer.readUuid();
        netId = buffer.readInt();
        packetDeletesGraph = buffer.readBoolean();
        fullWipe = buffer.readBoolean();

        if (packetDeletesGraph)
            return;

        DimensionPalette dimensions = DimensionPalette.PACKET_CODEC.decode(buffer);

        addedNodes = new HashMap<>();
        addedEdges = new ArrayList<>();
        addedEdgePoints = new ArrayList<>();
        removedEdgePoints = new ArrayList<>();
        removedNodes = new ArrayList<>();
        splitSubGraphs = new HashMap<>();
        updatedEdgeData = new HashMap<>();

        size = buffer.readVarInt();
        for (int i = 0; i < size; i++)
            removedNodes.add(buffer.readVarInt());

        size = buffer.readVarInt();
        for (int i = 0; i < size; i++)
            addedNodes.put(buffer.readVarInt(), Pair.of(TrackNodeLocation.receive(buffer, dimensions), VecHelper.read(buffer)));

        size = buffer.readVarInt();
        for (int i = 0; i < size; i++)
            addedEdges.add(Pair.of(
                Pair.of(Couple.create(buffer::readVarInt), TrackMaterial.PACKET_CODEC.decode(buffer)),
                buffer.readBoolean() ? new BezierConnection(buffer) : null
            ));

        size = buffer.readVarInt();
        for (int i = 0; i < size; i++)
            addedEdgePoints.add(EdgePointType.read(buffer, dimensions));

        size = buffer.readVarInt();
        for (int i = 0; i < size; i++)
            removedEdgePoints.add(buffer.readUuid());

        size = buffer.readVarInt();
        for (int i = 0; i < size; i++) {
            ArrayList<UUID> list = new ArrayList<>();
            Couple<Integer> key = Couple.create(buffer::readInt);
            Pair<Integer, List<UUID>> entry = Pair.of(buffer.readVarInt(), list);
            int size2 = buffer.readVarInt();
            for (int j = 0; j < size2; j++)
                list.add(buffer.readUuid());
            updatedEdgeData.put(key, entry);
        }

        size = buffer.readVarInt();
        for (int i = 0; i < size; i++)
            splitSubGraphs.put(buffer.readVarInt(), Pair.of(buffer.readInt(), buffer.readUuid()));
    }

    public void write(PacketByteBuf buffer) {
        buffer.writeUuid(graphId);
        buffer.writeInt(netId);
        buffer.writeBoolean(packetDeletesGraph);
        buffer.writeBoolean(fullWipe);

        if (packetDeletesGraph)
            return;

        // Populate and send palette ahead of time
        DimensionPalette dimensions = new DimensionPalette();
        addedNodes.forEach((node, loc) -> dimensions.encode(loc.getFirst().dimension));
        addedEdgePoints.forEach(ep -> ep.edgeLocation.forEach(loc -> dimensions.encode(loc.dimension)));
        DimensionPalette.PACKET_CODEC.encode(buffer, dimensions);

        buffer.writeVarInt(removedNodes.size());
        removedNodes.forEach(buffer::writeVarInt);

        buffer.writeVarInt(addedNodes.size());
        addedNodes.forEach((node, loc) -> {
            buffer.writeVarInt(node);
            loc.getFirst().send(buffer, dimensions);
            Vec3d.PACKET_CODEC.encode(buffer, loc.getSecond());
        });

        buffer.writeVarInt(addedEdges.size());
        addedEdges.forEach(pair -> {
            pair.getFirst().getFirst().forEach(buffer::writeVarInt);
            TrackMaterial.PACKET_CODEC.encode(buffer, pair.getFirst().getSecond());
            BezierConnection turn = pair.getSecond();
            buffer.writeBoolean(turn != null);
            if (turn != null)
                turn.write(buffer);
        });

        buffer.writeVarInt(addedEdgePoints.size());
        addedEdgePoints.forEach(ep -> ep.write(buffer, dimensions));

        buffer.writeVarInt(removedEdgePoints.size());
        removedEdgePoints.forEach(buffer::writeUuid);

        buffer.writeVarInt(updatedEdgeData.size());
        for (Map.Entry<Couple<Integer>, Pair<Integer, List<UUID>>> entry : updatedEdgeData.entrySet()) {
            entry.getKey().forEach(buffer::writeInt);
            Pair<Integer, List<UUID>> pair = entry.getValue();
            buffer.writeVarInt(pair.getFirst());
            List<UUID> list = pair.getSecond();
            buffer.writeVarInt(list.size());
            list.forEach(buffer::writeUuid);
        }

        buffer.writeVarInt(splitSubGraphs.size());
        splitSubGraphs.forEach((node, p) -> {
            buffer.writeVarInt(node);
            buffer.writeInt(p.getFirst());
            buffer.writeUuid(p.getSecond());
        });
    }

    public void syncEdgeData(TrackNode node1, TrackNode node2, TrackEdge edge) {
        Couple<Integer> key = Couple.create(node1.getNetId(), node2.getNetId());
        List<UUID> list = new ArrayList<>();
        EdgeData edgeData = edge.getEdgeData();
        int groupType = edgeData.hasSignalBoundaries() ? NULL_GROUP : EdgeData.passiveGroup.equals(edgeData.getSingleSignalGroup()) ? PASSIVE_GROUP : GROUP;
        if (groupType == GROUP)
            list.add(edgeData.getSingleSignalGroup());
        for (TrackEdgePoint point : edgeData.getPoints())
            list.add(point.getId());
        updatedEdgeData.put(key, Pair.of(groupType, list));
    }
}
