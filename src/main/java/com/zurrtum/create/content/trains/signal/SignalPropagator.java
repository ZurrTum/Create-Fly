package com.zurrtum.create.content.trains.signal;

import com.google.common.base.Predicates;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.graph.*;
import net.minecraft.server.MinecraftServer;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class SignalPropagator {

    public static void onSignalRemoved(MinecraftServer server, TrackGraph graph, SignalBoundary signal) {
        signal.sidesToUpdate.map($ -> false);
        for (boolean front : Iterate.trueAndFalse) {
            if (signal.sidesToUpdate.get(front))
                continue;
            UUID id = signal.groups.get(front);
            if (Create.RAILWAYS.signalEdgeGroups.remove(id) != null)
                Create.RAILWAYS.sync.edgeGroupRemoved(server, id);

            walkSignals(
                server, graph, signal, front, pair -> {
                    TrackNode node1 = pair.getFirst();
                    SignalBoundary boundary = pair.getSecond();
                    boundary.queueUpdate(node1);
                    return false;

                }, signalData -> {
                    if (!signalData.hasSignalBoundaries()) {
                        signalData.setSingleSignalGroup(server, graph, EdgeData.passiveGroup);
                        return true;
                    }
                    return false;

                }, false
            );
        }
    }

    public static void notifySignalsOfNewNode(MinecraftServer server, TrackGraph graph, TrackNode node) {
        List<Couple<@Nullable TrackNode>> frontier = new ArrayList<>();
        frontier.add(Couple.create(node, null));
        walkSignals(
            server, graph, frontier, pair -> {
                TrackNode node1 = pair.getFirst();
                SignalBoundary boundary = pair.getSecond();
                boundary.queueUpdate(node1);
                return false;

            }, signalData -> {
                if (!signalData.hasSignalBoundaries()) {
                    signalData.setSingleSignalGroup(server, graph, EdgeData.passiveGroup);
                    return true;
                }
                return false;

            }, false
        );
    }

    public static void propagateSignalGroup(MinecraftServer server, TrackGraph graph, SignalBoundary signal, boolean front) {
        Map<UUID, SignalEdgeGroup> globalGroups = Create.RAILWAYS.signalEdgeGroups;
        TrackGraphSync sync = Create.RAILWAYS.sync;

        SignalEdgeGroup group = new SignalEdgeGroup(UUID.randomUUID());
        UUID groupId = group.id;
        globalGroups.put(groupId, group);
        signal.setGroup(front, groupId);
        sync.pointAdded(graph, signal);

        walkSignals(
            server, graph, signal, front, pair -> {
                TrackNode node1 = pair.getFirst();
                SignalBoundary boundary = pair.getSecond();
                UUID currentGroup = boundary.getGroup(node1);
                if (currentGroup != null)
                    if (globalGroups.remove(currentGroup) != null)
                        sync.edgeGroupRemoved(server, currentGroup);
                boundary.setGroupAndUpdate(node1, groupId);
                sync.pointAdded(graph, boundary);
                return true;

            }, signalData -> {
                UUID singleSignalGroup = signalData.getSingleSignalGroup();
                if (singleSignalGroup != null)
                    if (globalGroups.remove(singleSignalGroup) != null)
                        sync.edgeGroupRemoved(server, singleSignalGroup);
                signalData.setSingleSignalGroup(server, graph, groupId);
                return true;

            }, false
        );

        group.resolveColor(server);
        sync.edgeGroupCreated(server, groupId, group.color);
    }

    public static Map<UUID, Boolean> collectChainedSignals(MinecraftServer server, TrackGraph graph, SignalBoundary signal, boolean front) {
        HashMap<UUID, Boolean> map = new HashMap<>();
        walkSignals(
            server, graph, signal, front, pair -> {
                SignalBoundary boundary = pair.getSecond();
                map.put(boundary.id, !boundary.isPrimary(pair.getFirst()));
                return false;
            }, Predicates.alwaysFalse(), true
        );
        return map;
    }

    public static void walkSignals(
        MinecraftServer server,
        TrackGraph graph,
        SignalBoundary signal,
        boolean front,
        Predicate<Pair<TrackNode, SignalBoundary>> boundaryCallback,
        Predicate<EdgeData> nonBoundaryCallback,
        boolean forCollection
    ) {

        Couple<TrackNodeLocation> edgeLocation = signal.edgeLocation;
        Couple<TrackNode> startNodes = edgeLocation.map(graph::locateNode);
        Couple<TrackEdge> startEdges = startNodes.mapWithParams((l1, l2) -> graph.getConnectionsFrom(l1).get(l2), startNodes.swap());

        TrackNode node1 = startNodes.get(front);
        TrackNode node2 = startNodes.get(!front);
        TrackEdge startEdge = startEdges.get(front);
        TrackEdge oppositeEdge = startEdges.get(!front);

        if (startEdge == null)
            return;

        if (!forCollection) {
            notifyTrains(graph, startEdge, oppositeEdge);
            startEdge.getEdgeData().refreshIntersectingSignalGroups(server, graph);
            Create.RAILWAYS.sync.edgeDataChanged(graph, node1, node2, startEdge, oppositeEdge);
        }

        // Check for signal on the same edge
        SignalBoundary immediateBoundary = startEdge.getEdgeData().next(EdgePointType.SIGNAL, signal.getLocationOn(startEdge));
        if (immediateBoundary != null) {
            if (boundaryCallback.test(Pair.of(node1, immediateBoundary)))
                startEdge.getEdgeData().refreshIntersectingSignalGroups(server, graph);
            return;
        }

        // Search for any connected signals
        List<Couple<TrackNode>> frontier = new ArrayList<>();
        frontier.add(Couple.create(node2, node1));
        walkSignals(server, graph, frontier, boundaryCallback, nonBoundaryCallback, forCollection);
    }

    private static void walkSignals(
        MinecraftServer server,
        TrackGraph graph,
        List<Couple<TrackNode>> frontier,
        Predicate<Pair<TrackNode, SignalBoundary>> boundaryCallback,
        Predicate<EdgeData> nonBoundaryCallback,
        boolean forCollection
    ) {
        Set<TrackEdge> visited = new HashSet<>();
        while (!frontier.isEmpty()) {
            Couple<TrackNode> couple = frontier.removeFirst();
            TrackNode currentNode = couple.getFirst();
            TrackNode prevNode = couple.getSecond();

            EdgeWalk:
            for (Map.Entry<TrackNode, TrackEdge> entry : graph.getConnectionsFrom(currentNode).entrySet()) {
                TrackNode nextNode = entry.getKey();
                TrackEdge edge = entry.getValue();

                if (nextNode == prevNode)
                    continue;

                // already checked this edge
                if (!visited.add(edge))
                    continue;

                // chain signal: check if reachable
                if (forCollection && !graph.getConnectionsFrom(prevNode).get(currentNode).canTravelTo(edge))
                    continue;

                TrackEdge oppositeEdge = graph.getConnectionsFrom(nextNode).get(currentNode);
                visited.add(oppositeEdge);

                for (boolean flip : Iterate.falseAndTrue) {
                    TrackEdge currentEdge = flip ? oppositeEdge : edge;
                    EdgeData signalData = currentEdge.getEdgeData();

                    // no boundary- update group of edge
                    if (!signalData.hasSignalBoundaries()) {
                        if (nonBoundaryCallback.test(signalData)) {
                            notifyTrains(graph, currentEdge);
                            Create.RAILWAYS.sync.edgeDataChanged(graph, currentNode, nextNode, edge, oppositeEdge);
                        }
                        continue;
                    }

                    // other/own boundary found
                    SignalBoundary nextBoundary = signalData.next(EdgePointType.SIGNAL, 0);
                    if (nextBoundary == null)
                        continue;
                    if (boundaryCallback.test(Pair.of(currentNode, nextBoundary))) {
                        notifyTrains(graph, edge, oppositeEdge);
                        currentEdge.getEdgeData().refreshIntersectingSignalGroups(server, graph);
                        Create.RAILWAYS.sync.edgeDataChanged(graph, currentNode, nextNode, edge, oppositeEdge);
                    }
                    continue EdgeWalk;
                }

                frontier.add(Couple.create(nextNode, currentNode));
            }
        }
    }

    public static void notifyTrains(TrackGraph graph, TrackEdge... edges) {
        for (TrackEdge trackEdge : edges) {
            for (Train train : Create.RAILWAYS.trains.values()) {
                if (train.graph != graph)
                    continue;
                if (train.updateSignalBlocks)
                    continue;
                train.forEachTravellingPoint(tp -> {
                    if (tp.edge == trackEdge)
                        train.updateSignalBlocks = true;
                });
            }
        }
    }

}
