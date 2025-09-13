package com.zurrtum.create.content.trains.graph;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.trains.signal.EdgeGroupColor;
import com.zurrtum.create.content.trains.signal.TrackEdgePoint;
import com.zurrtum.create.infrastructure.packet.s2c.SignalEdgeGroupPacket;
import com.zurrtum.create.infrastructure.packet.s2c.TrackGraphPacket;
import com.zurrtum.create.infrastructure.packet.s2c.TrackGraphRollCallPacket;
import com.zurrtum.create.infrastructure.packet.s2c.TrackGraphSyncPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TrackGraphSync {

    List<TrackGraphPacket> queuedPackets = new ArrayList<>();
    int rollCallIn;

    public void serverTick(MinecraftServer server) {
        flushGraphPacket();

        if (!queuedPackets.isEmpty()) {
            for (TrackGraphPacket packet : queuedPackets) {
                if (!packet.packetDeletesGraph && !Create.RAILWAYS.trackNetworks.containsKey(packet.graphId))
                    continue;
                server.getPlayerManager().sendToAll(packet);
                rollCallIn = 3;
            }

            queuedPackets.clear();
        }

        if (rollCallIn <= 0)
            return;
        rollCallIn--;
        if (rollCallIn > 0)
            return;

        sendRollCall(server);
    }

    //

    public void nodeAdded(TrackGraph graph, TrackNode node) {
        flushGraphPacket(graph);
        currentGraphSyncPacket.addedNodes.put(node.getNetId(), Pair.of(node.getLocation(), node.getNormal()));
        currentPayload++;
    }

    public void edgeAdded(TrackGraph graph, TrackNode node1, TrackNode node2, TrackEdge edge) {
        flushGraphPacket(graph);
        currentGraphSyncPacket.addedEdges.add(Pair.of(
            Pair.of(Couple.create(node1.getNetId(), node2.getNetId()), edge.getTrackMaterial()),
            edge.getTurn()
        ));
        currentPayload++;
    }

    public void pointAdded(TrackGraph graph, TrackEdgePoint point) {
        flushGraphPacket(graph);
        currentGraphSyncPacket.addedEdgePoints.add(point);
        currentPayload++;
    }

    public void pointRemoved(TrackGraph graph, TrackEdgePoint point) {
        flushGraphPacket(graph);
        currentGraphSyncPacket.removedEdgePoints.add(point.getId());
        currentPayload++;
    }

    public void nodeRemoved(TrackGraph graph, TrackNode node) {
        flushGraphPacket(graph);
        int nodeId = node.getNetId();
        if (currentGraphSyncPacket.addedNodes.remove(nodeId) == null)
            currentGraphSyncPacket.removedNodes.add(nodeId);
        currentGraphSyncPacket.addedEdges.removeIf(pair -> {
            Couple<Integer> ids = pair.getFirst().getFirst();
            return ids.getFirst().intValue() == nodeId || ids.getSecond().intValue() == nodeId;
        });
    }

    public void graphSplit(TrackGraph graph, Set<TrackGraph> additional) {
        flushGraphPacket(graph);
        additional.forEach(rg -> currentGraphSyncPacket.splitSubGraphs.put(
            rg.nodesById.keySet().stream().findFirst().get(),
            Pair.of(rg.netId, rg.id)
        ));
    }

    public void graphRemoved(TrackGraph graph) {
        flushGraphPacket(graph);
        currentGraphSyncPacket.packetDeletesGraph = true;
    }

    //

    public void sendEdgeGroups(List<UUID> ids, List<EdgeGroupColor> colors, ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new SignalEdgeGroupPacket(ids, colors, true));
    }

    public void edgeGroupCreated(MinecraftServer server, UUID id, EdgeGroupColor color) {
        server.getPlayerManager().sendToAll(new SignalEdgeGroupPacket(id, color));
    }

    public void edgeGroupRemoved(MinecraftServer server, UUID id) {
        server.getPlayerManager().sendToAll(new SignalEdgeGroupPacket(ImmutableList.of(id), Collections.emptyList(), false));
    }

    //

    public void edgeDataChanged(TrackGraph graph, TrackNode node1, TrackNode node2, TrackEdge edge) {
        flushGraphPacket(graph);
        currentGraphSyncPacket.syncEdgeData(node1, node2, edge);
        currentPayload++;
    }

    public void edgeDataChanged(TrackGraph graph, TrackNode node1, TrackNode node2, TrackEdge edge, TrackEdge edge2) {
        flushGraphPacket(graph);
        currentGraphSyncPacket.syncEdgeData(node1, node2, edge);
        currentGraphSyncPacket.syncEdgeData(node2, node1, edge2);
        currentPayload++;
    }

    public void sendFullGraphTo(TrackGraph graph, ServerPlayerEntity player) {
        TrackGraphSyncPacket packet = new TrackGraphSyncPacket(graph.id, graph.netId);
        packet.fullWipe = true;
        int sent = 0;

        Set<TrackEdgePoint> sentPoints = new HashSet<>();

        for (TrackNode node : graph.nodes.values()) {
            TrackGraphSyncPacket currentPacket = packet;
            currentPacket.addedNodes.put(node.getNetId(), Pair.of(node.getLocation(), node.getNormal()));
            if (sent++ < 1000)
                continue;

            sent = 0;
            packet = flushAndCreateNew(graph, player, packet);
        }

        for (TrackNode node : graph.nodes.values()) {
            TrackGraphSyncPacket currentPacket = packet;
            if (!graph.connectionsByNode.containsKey(node))
                continue;

            for (Map.Entry<TrackNode, TrackEdge> entry : graph.connectionsByNode.get(node).entrySet()) {
                TrackNode node2 = entry.getKey();
                TrackEdge edge = entry.getValue();

                Couple<Integer> key = Couple.create(node.getNetId(), node2.getNetId());
                currentPacket.addedEdges.add(Pair.of(Pair.of(key, edge.getTrackMaterial()), edge.getTurn()));
                currentPacket.syncEdgeData(node, node2, edge);

                for (TrackEdgePoint point : edge.edgeData.getPoints()) {
                    if (sentPoints.contains(point))
                        continue;

                    sentPoints.add(point);
                    currentPacket.addedEdgePoints.add(point);
                    sent++;
                }
            }

            if (sent++ < 1000)
                continue;

            sent = 0;
            packet = flushAndCreateNew(graph, player, packet);
        }

        for (EdgePointType<?> type : EdgePointType.TYPES.values()) {
            for (TrackEdgePoint point : graph.getPoints(type)) {
                if (sentPoints.contains(point))
                    continue;

                sentPoints.add(point);
                packet.addedEdgePoints.add(point);

                if (sent++ < 1000)
                    continue;

                sent = 0;
                packet = flushAndCreateNew(graph, player, packet);
            }
        }

        if (sent > 0)
            flushAndCreateNew(graph, player, packet);
    }

    private void sendRollCall(MinecraftServer server) {
        server.getPlayerManager().sendToAll(TrackGraphRollCallPacket.ofServer());
    }

    private TrackGraphSyncPacket flushAndCreateNew(TrackGraph graph, ServerPlayerEntity player, TrackGraphSyncPacket packet) {
        player.networkHandler.sendPacket(packet);
        packet = new TrackGraphSyncPacket(graph.id, graph.netId);
        return packet;
    }

    //

    private TrackGraphSyncPacket currentGraphSyncPacket;
    private int currentPayload;

    private void flushGraphPacket() {
        flushGraphPacket(null, 0);
    }

    private void flushGraphPacket(TrackGraph graph) {
        flushGraphPacket(graph.id, graph.netId);
    }

    private void flushGraphPacket(@Nullable UUID graphId, int netId) {
        if (currentGraphSyncPacket != null) {
            if (currentGraphSyncPacket.graphId.equals(graphId) && currentPayload < 1000)
                return;
            queuedPackets.add(currentGraphSyncPacket);
            currentGraphSyncPacket = null;
            currentPayload = 0;
        }

        if (graphId != null) {
            currentGraphSyncPacket = new TrackGraphSyncPacket(graphId, netId);
            currentPayload = 0;
        }
    }

}
