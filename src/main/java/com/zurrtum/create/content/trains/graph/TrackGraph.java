package com.zurrtum.create.content.trains.graph;

import com.mojang.serialization.*;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.graph.TrackNodeLocation.DiscoveredLocation;
import com.zurrtum.create.content.trains.signal.SignalEdgeGroup;
import com.zurrtum.create.content.trains.signal.TrackEdgePoint;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TrackGraph {

    public static final AtomicInteger graphNetIdGenerator = new AtomicInteger();
    public static final AtomicInteger nodeNetIdGenerator = new AtomicInteger();

    public UUID id;
    public Color color;

    public Map<TrackNodeLocation, TrackNode> nodes;
    Map<Integer, TrackNode> nodesById;
    public Map<TrackNode, Map<TrackNode, TrackEdge>> connectionsByNode;
    public EdgePointStorage edgePoints;
    Map<ResourceKey<Level>, TrackGraphBounds> bounds;

    List<TrackEdge> deferredIntersectionUpdates;

    public int netId;
    int checksum = 0;

    public TrackGraph() {
        this(UUID.randomUUID());
    }

    public TrackGraph(UUID graphID) {
        setId(graphID);
        nodes = new HashMap<>();
        nodesById = new HashMap<>();
        bounds = new HashMap<>();
        connectionsByNode = new IdentityHashMap<>();
        edgePoints = new EdgePointStorage();
        deferredIntersectionUpdates = new ArrayList<>();
        netId = nextGraphId();
    }

    //

    public <T extends TrackEdgePoint> void addPoint(MinecraftServer server, EdgePointType<T> type, T point) {
        edgePoints.put(type, point);
        EdgePointManager.onEdgePointAdded(server, this, point, type);
        Create.RAILWAYS.sync.pointAdded(this, point);
        markDirty();
    }

    public <T extends TrackEdgePoint> T getPoint(EdgePointType<T> type, UUID id) {
        return edgePoints.get(type, id);
    }

    public <T extends TrackEdgePoint> Collection<T> getPoints(EdgePointType<T> type) {
        return edgePoints.values(type);
    }

    public <T extends TrackEdgePoint> T removePoint(MinecraftServer server, EdgePointType<T> type, UUID id) {
        T removed = edgePoints.remove(type, id);
        if (removed == null)
            return null;
        EdgePointManager.onEdgePointRemoved(server, this, removed, type);
        Create.RAILWAYS.sync.pointRemoved(this, removed);
        markDirty();
        return removed;
    }

    public void tickPoints(MinecraftServer server, boolean preTrains) {
        edgePoints.tick(server, this, preTrains);
    }

    //

    public TrackGraphBounds getBounds(Level level) {
        return bounds.computeIfAbsent(level.dimension(), dim -> new TrackGraphBounds(this, dim));
    }

    public void invalidateBounds() {
        checksum = 0;
        bounds.clear();
    }

    //

    public Set<TrackNodeLocation> getNodes() {
        return nodes.keySet();
    }

    public TrackNode locateNode(Level level, Vec3 position) {
        return locateNode(new TrackNodeLocation(position).in(level));
    }

    public TrackNode locateNode(TrackNodeLocation position) {
        return nodes.get(position);
    }

    public TrackNode getNode(int netId) {
        return nodesById.get(netId);
    }

    public boolean createNodeIfAbsent(DiscoveredLocation location) {
        if (!addNodeIfAbsent(new TrackNode(location, nextNodeId(), location.normal)))
            return false;
        TrackNode newNode = nodes.get(location);
        Create.RAILWAYS.sync.nodeAdded(this, newNode);
        invalidateBounds();
        markDirty();
        return true;
    }

    public void loadNode(TrackNodeLocation location, int netId, Vec3 normal) {
        addNode(new TrackNode(location, netId, normal));
    }

    public void addNode(TrackNode node) {
        TrackNodeLocation location = node.getLocation();
        if (nodes.containsKey(location))
            removeNode(null, location);
        nodes.put(location, node);
        nodesById.put(node.getNetId(), node);
    }

    public boolean addNodeIfAbsent(TrackNode node) {
        if (nodes.putIfAbsent(node.getLocation(), node) != null)
            return false;
        nodesById.put(node.getNetId(), node);
        return true;
    }

    public boolean removeNode(@Nullable LevelAccessor level, TrackNodeLocation location) {
        TrackNode removed = nodes.remove(location);
        if (removed == null)
            return false;

        Map<UUID, Train> trains = Create.RAILWAYS.trains;
        for (UUID uuid : trains.keySet()) {
            Train train = trains.get(uuid);
            if (train.graph != this)
                continue;
            if (train.isTravellingOn(removed))
                train.detachFromTracks();
        }

        nodesById.remove(removed.netId);
        invalidateBounds();

        if (!connectionsByNode.containsKey(removed))
            return true;

        Map<TrackNode, TrackEdge> connections = connectionsByNode.remove(removed);
        MinecraftServer server = level != null ? level.getServer() : null;
        for (Map.Entry<TrackNode, TrackEdge> entry : connections.entrySet()) {
            TrackEdge trackEdge = entry.getValue();
            EdgeData edgeData = trackEdge.getEdgeData();
            for (TrackEdgePoint point : edgeData.getPoints()) {
                if (level != null)
                    point.invalidate(level);
                edgePoints.remove(point.getType(), point.getId());
            }
            if (level != null) {
                for (TrackEdgeIntersection intersection : edgeData.getIntersections()) {
                    Couple<TrackNodeLocation> target = intersection.target;
                    TrackGraph graph = Create.RAILWAYS.getGraph(target.getFirst());
                    if (graph != null)
                        graph.removeIntersection(server, intersection);
                }
            }
        }

        for (TrackNode railNode : connections.keySet())
            if (connectionsByNode.containsKey(railNode))
                connectionsByNode.get(railNode).remove(removed);

        return true;
    }

    private void removeIntersection(MinecraftServer server, TrackEdgeIntersection intersection) {
        TrackNode node1 = locateNode(intersection.target.getFirst());
        TrackNode node2 = locateNode(intersection.target.getSecond());
        if (node1 == null || node2 == null)
            return;

        Map<TrackNode, TrackEdge> from1 = getConnectionsFrom(node1);
        if (from1 != null) {
            TrackEdge edge = from1.get(node2);
            if (edge != null)
                edge.getEdgeData().removeIntersection(server, this, intersection.id);
        }

        Map<TrackNode, TrackEdge> from2 = getConnectionsFrom(node2);
        if (from2 != null) {
            TrackEdge edge = from2.get(node1);
            if (edge != null)
                edge.getEdgeData().removeIntersection(server, this, intersection.id);
        }
    }

    public static int nextNodeId() {
        return nodeNetIdGenerator.incrementAndGet();
    }

    public static int nextGraphId() {
        return graphNetIdGenerator.incrementAndGet();
    }

    public void transferAll(TrackGraph toOther) {
        nodes.forEach((loc, node) -> {
            if (toOther.addNodeIfAbsent(node))
                Create.RAILWAYS.sync.nodeAdded(toOther, node);
        });

        connectionsByNode.forEach((node1, map) -> map.forEach((node2, edge) -> {
            TrackNode n1 = toOther.locateNode(node1.location);
            TrackNode n2 = toOther.locateNode(node2.location);
            if (n1 == null || n2 == null)
                return;
            if (toOther.putConnection(n1, n2, edge)) {
                Create.RAILWAYS.sync.edgeAdded(toOther, n1, n2, edge);
                Create.RAILWAYS.sync.edgeDataChanged(toOther, n1, n2, edge);
            }
        }));

        edgePoints.transferAll(toOther, toOther.edgePoints);
        nodes.clear();
        connectionsByNode.clear();
        toOther.invalidateBounds();

        Map<UUID, Train> trains = Create.RAILWAYS.trains;
        for (UUID uuid : trains.keySet()) {
            Train train = trains.get(uuid);
            if (train.graph != this)
                continue;
            train.graph = toOther;
        }
    }

    public Set<TrackGraph> findDisconnectedGraphs(@Nullable LevelAccessor level, @Nullable Map<Integer, Pair<Integer, UUID>> splitSubGraphs) {
        Set<TrackGraph> dicovered = new HashSet<>();
        Set<TrackNodeLocation> vertices = new HashSet<>(nodes.keySet());
        List<TrackNodeLocation> frontier = new ArrayList<>();
        TrackGraph target = null;

        while (!vertices.isEmpty()) {
            if (target != null)
                dicovered.add(target);

            TrackNodeLocation start = vertices.stream().findFirst().get();
            frontier.add(start);
            vertices.remove(start);

            while (!frontier.isEmpty()) {
                TrackNodeLocation current = frontier.remove(0);
                TrackNode currentNode = locateNode(current);

                Map<TrackNode, TrackEdge> connections = getConnectionsFrom(currentNode);
                for (TrackNode connected : connections.keySet())
                    if (vertices.remove(connected.getLocation()))
                        frontier.add(connected.getLocation());

                if (target != null) {
                    if (splitSubGraphs != null && splitSubGraphs.containsKey(currentNode.getNetId())) {
                        Pair<Integer, UUID> ids = splitSubGraphs.get(currentNode.getNetId());
                        target.setId(ids.getSecond());
                        target.netId = ids.getFirst();
                    }
                    transfer(level, currentNode, target);
                }
            }

            frontier.clear();
            target = new TrackGraph();
        }

        return dicovered;
    }

    public void setId(UUID id) {
        this.id = id;
        color = Color.rainbowColor(new Random(id.getLeastSignificantBits()).nextInt());
    }

    public void setNetId(int id) {
        this.netId = id;
    }

    public int getChecksum() {
        if (checksum == 0)
            checksum = nodes.values().stream().collect(Collectors.summingInt(TrackNode::getNetId));
        return checksum;
    }

    public void transfer(LevelAccessor level, TrackNode node, TrackGraph target) {
        target.addNode(node);
        target.invalidateBounds();

        TrackNodeLocation nodeLoc = node.getLocation();
        Map<TrackNode, TrackEdge> connections = getConnectionsFrom(node);
        Map<UUID, Train> trains = Create.RAILWAYS.sided(level).trains;

        if (!connections.isEmpty()) {
            target.connectionsByNode.put(node, connections);
            for (TrackEdge entry : connections.values()) {
                EdgeData edgeData = entry.getEdgeData();
                for (TrackEdgePoint trackEdgePoint : edgeData.getPoints()) {
                    target.edgePoints.put(trackEdgePoint.getType(), trackEdgePoint);
                    edgePoints.remove(trackEdgePoint.getType(), trackEdgePoint.getId());
                }
            }
        }

        if (level != null)
            for (UUID uuid : trains.keySet()) {
                Train train = trains.get(uuid);
                if (train.graph != this)
                    continue;
                if (!train.isTravellingOn(node))
                    continue;
                train.graph = target;
            }

        nodes.remove(nodeLoc);
        nodesById.remove(node.getNetId());
        connectionsByNode.remove(node);
        invalidateBounds();
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public Map<TrackNode, TrackEdge> getConnectionsFrom(TrackNode node) {
        if (node == null)
            return null;
        return connectionsByNode.getOrDefault(node, new HashMap<>());
    }

    public TrackEdge getConnection(Couple<TrackNode> nodes) {
        Map<TrackNode, TrackEdge> connectionsFrom = getConnectionsFrom(nodes.getFirst());
        if (connectionsFrom == null)
            return null;
        return connectionsFrom.get(nodes.getSecond());
    }

    public void connectNodes(LevelAccessor reader, DiscoveredLocation location, DiscoveredLocation location2, @Nullable BezierConnection turn) {
        TrackNode node1 = nodes.get(location);
        TrackNode node2 = nodes.get(location2);

        boolean bezier = turn != null;
        TrackMaterial material = bezier ? turn.getMaterial() : location2.materialA;
        TrackEdge edge = new TrackEdge(node1, node2, turn, material);
        TrackEdge edge2 = new TrackEdge(node2, node1, bezier ? turn.secondary() : null, material);

        for (TrackGraph graph : Create.RAILWAYS.trackNetworks.values()) {
            for (TrackNode otherNode1 : graph.nodes.values()) {
                Map<TrackNode, TrackEdge> connections = graph.connectionsByNode.get(otherNode1);
                if (connections == null)
                    continue;
                for (Map.Entry<TrackNode, TrackEdge> entry : connections.entrySet()) {
                    TrackNode otherNode2 = entry.getKey();
                    TrackEdge otherEdge = entry.getValue();

                    if (graph == this)
                        if (otherNode1 == node1 || otherNode2 == node1 || otherNode1 == node2 || otherNode2 == node2)
                            continue;

                    if (edge == otherEdge)
                        continue;
                    if (otherEdge.isInterDimensional() || edge.isInterDimensional())
                        continue;
                    if (node1.location.dimension != otherNode1.location.dimension)
                        continue;
                    if (!bezier && !otherEdge.isTurn())
                        continue;
                    if (otherEdge.isTurn() && otherEdge.turn.isPrimary())
                        continue;

                    Collection<double[]> intersections = edge.getIntersection(node1, node2, otherEdge, otherNode1, otherNode2);

                    UUID id = UUID.randomUUID();
                    for (double[] intersection : intersections) {
                        double s = intersection[0];
                        double t = intersection[1];
                        edge.edgeData.addIntersection(this, id, s, otherNode1, otherNode2, t);
                        edge2.edgeData.addIntersection(this, id, edge.getLength() - s, otherNode1, otherNode2, t);
                        otherEdge.edgeData.addIntersection(graph, id, t, node1, node2, s);
                        TrackEdge otherEdge2 = graph.getConnection(Couple.create(otherNode2, otherNode1));
                        if (otherEdge2 != null)
                            otherEdge2.edgeData.addIntersection(graph, id, otherEdge.getLength() - t, node1, node2, s);
                    }
                }
            }
        }

        putConnection(node1, node2, edge);
        putConnection(node2, node1, edge2);
        Create.RAILWAYS.sync.edgeAdded(this, node1, node2, edge);
        Create.RAILWAYS.sync.edgeAdded(this, node2, node1, edge2);

        markDirty();
    }

    public void disconnectNodes(TrackNode node1, TrackNode node2) {
        Map<TrackNode, TrackEdge> map1 = connectionsByNode.get(node1);
        Map<TrackNode, TrackEdge> map2 = connectionsByNode.get(node2);
        if (map1 != null)
            map1.remove(node2);
        if (map2 != null)
            map2.remove(node1);
    }

    public boolean putConnection(TrackNode node1, TrackNode node2, TrackEdge edge) {
        Map<TrackNode, TrackEdge> connections = connectionsByNode.computeIfAbsent(node1, n -> new IdentityHashMap<>());
        if (connections.containsKey(node2) && connections.get(node2).getEdgeData().hasPoints())
            return false;
        return connections.put(node2, edge) == null;
    }

    public float distanceToLocationSqr(Level level, Vec3 location) {
        float nearest = Float.MAX_VALUE;
        for (TrackNodeLocation tnl : nodes.keySet()) {
            if (!Objects.equals(tnl.dimension, level.dimension()))
                continue;
            nearest = Math.min(nearest, (float) tnl.getLocation().distanceToSqr(location));
        }
        return nearest;
    }

    public void deferIntersectionUpdate(TrackEdge edge) {
        deferredIntersectionUpdates.add(edge);
    }

    public void resolveIntersectingEdgeGroups(Level level) {
        MinecraftServer server = level.getServer();
        for (TrackEdge edge : deferredIntersectionUpdates) {
            if (!connectionsByNode.containsKey(edge.node1) || edge != connectionsByNode.get(edge.node1).get(edge.node2))
                continue;
            EdgeData edgeData = edge.getEdgeData();
            for (TrackEdgeIntersection intersection : edgeData.getIntersections()) {
                UUID groupId = edgeData.getGroupAtPosition(this, intersection.location);
                Couple<TrackNodeLocation> target = intersection.target;
                TrackGraph graph = Create.RAILWAYS.getGraph(target.getFirst());
                if (graph == null)
                    continue;

                TrackNode node1 = graph.locateNode(target.getFirst());
                TrackNode node2 = graph.locateNode(target.getSecond());
                Map<TrackNode, TrackEdge> connectionsFrom = graph.getConnectionsFrom(node1);
                if (connectionsFrom == null)
                    continue;
                TrackEdge otherEdge = connectionsFrom.get(node2);
                if (otherEdge == null)
                    continue;
                UUID otherGroupId = otherEdge.getEdgeData().getGroupAtPosition(graph, intersection.targetLocation);

                SignalEdgeGroup group = Create.RAILWAYS.signalEdgeGroups.get(groupId);
                SignalEdgeGroup otherGroup = Create.RAILWAYS.signalEdgeGroups.get(otherGroupId);
                if (group == null || otherGroup == null || groupId == null || otherGroupId == null)
                    continue;

                intersection.groupId = groupId;
                group.putIntersection(server, intersection.id, otherGroupId);
                otherGroup.putIntersection(server, intersection.id, groupId);
            }
        }
        deferredIntersectionUpdates.clear();
    }

    public void markDirty() {
        Create.RAILWAYS.markTracksDirty();
    }

    public void write(ValueOutput view, DimensionPalette dimensions) {
        view.store("Id", UUIDUtil.CODEC, id);
        view.putInt("Color", color.getRGB());

        Map<TrackNode, Integer> indexTracker = new HashMap<>();
        ValueOutput.ValueOutputList list = view.childrenList("Nodes");
        ValueOutput[] nodesList = new ValueOutput[nodes.size()];
        int i = 0;
        for (TrackNode railNode : nodes.values()) {
            indexTracker.put(railNode, i);
            ValueOutput node = list.addChild();
            railNode.getLocation().write(node.child("Location"), dimensions);
            node.store("Normal", Vec3.CODEC, railNode.getNormal());
            nodesList[i] = node;
            i++;
        }

        connectionsByNode.forEach((node1, map) -> {
            Integer index1 = indexTracker.get(node1);
            if (index1 == null)
                return;
            ValueOutput.ValueOutputList connections = nodesList[index1].childrenList("Connections");
            map.forEach((node2, edge) -> {
                Integer index2 = indexTracker.get(node2);
                if (index2 == null)
                    return;
                ValueOutput connection = connections.addChild();
                connection.putInt("To", index2);
                edge.write(connection.child("EdgeData"), dimensions);
            });
        });

        edgePoints.write(view.child("Points"), dimensions);
    }

    @SuppressWarnings("unchecked")
    public static <T> DataResult<T> encode(final TrackGraph input, final DynamicOps<T> ops, final T empty, DimensionPalette dimensions) {
        RecordBuilder<T> builder = ops.mapBuilder();
        builder.add("Id", input.id, UUIDUtil.CODEC);
        builder.add("Color", ops.createInt(input.color.getRGB()));

        Map<TrackNode, Integer> indexTracker = new HashMap<>();
        RecordBuilder<T>[] nodesList = new RecordBuilder[input.nodes.size()];
        int i = 0;
        for (TrackNode railNode : input.nodes.values()) {
            indexTracker.put(railNode, i);
            RecordBuilder<T> node = ops.mapBuilder();
            node.add("Location", TrackNodeLocation.encode(railNode.getLocation(), ops, empty, dimensions));
            node.add("Normal", railNode.getNormal(), Vec3.CODEC);
            nodesList[i] = node;
            i++;
        }
        input.connectionsByNode.forEach((node1, map) -> {
            Integer index1 = indexTracker.get(node1);
            if (index1 == null)
                return;
            RecordBuilder<T> node = nodesList[index1];
            ListBuilder<T> connections = ops.listBuilder();
            map.forEach((node2, edge) -> {
                Integer index2 = indexTracker.get(node2);
                if (index2 == null)
                    return;
                RecordBuilder<T> connection = ops.mapBuilder();
                connection.add("To", ops.createInt(index2));
                connection.add("EdgeData", TrackEdge.encode(edge, ops, empty, dimensions));
                connections.add(connection.build(empty));
            });
            node.add("Connections", connections.build(empty));
        });
        ListBuilder<T> list = ops.listBuilder();
        for (RecordBuilder<T> node : nodesList) {
            list.add(node.build(empty));
        }
        builder.add("Nodes", list.build(empty));
        builder.add("Points", EdgePointStorage.encode(input.edgePoints, ops, empty, dimensions));
        return builder.build(empty);
    }

    public static TrackGraph read(ValueInput view, DimensionPalette dimensions) {
        TrackGraph graph = new TrackGraph(view.read("Id", UUIDUtil.CODEC).orElseThrow());
        graph.color = new Color(view.getIntOr("Color", 0));
        graph.edgePoints.read(view.childOrEmpty("Points"), dimensions);

        Map<Integer, TrackNode> indexTracker = new HashMap<>();
        ValueInput.ValueInputList nodes = view.childrenListOrEmpty("Nodes");

        int i = 0;
        for (ValueInput node : nodes) {
            TrackNodeLocation location = TrackNodeLocation.read(node.childOrEmpty("Location"), dimensions);
            Vec3 normal = view.read("Normal", Vec3.CODEC).orElseThrow();
            graph.loadNode(location, nextNodeId(), normal);
            indexTracker.put(i, graph.locateNode(location));
            i++;
        }

        i = 0;
        for (ValueInput node : nodes) {
            TrackNode node1 = indexTracker.get(i);
            i++;

            node.childrenList("Connections").ifPresent(connections -> connections.forEach(connection -> {
                TrackNode node2 = indexTracker.get(connection.getIntOr("To", 0));
                TrackEdge edge = TrackEdge.read(node1, node2, connection.childOrEmpty("EdgeData"), graph, dimensions);
                graph.putConnection(node1, node2, edge);
            }));
        }

        return graph;
    }

    public static <T> TrackGraph decode(final DynamicOps<T> ops, final T input, DimensionPalette dimensions) {
        MapLike<T> map = ops.getMap(input).getOrThrow();
        TrackGraph graph = new TrackGraph(UUIDUtil.CODEC.decode(ops, map.get("Id")).getOrThrow().getFirst());
        graph.color = new Color(ops.getNumberValue(map.get("Color"), 0).intValue());
        graph.edgePoints.decode(ops, map.get("Points"), dimensions);

        Map<Integer, TrackNode> indexTracker = new HashMap<>();
        Map<Integer, MapLike<T>> nodes = new HashMap<>();
        MutableInt i = new MutableInt();
        ops.getList(map.get("Nodes")).getOrThrow().accept(item -> {
            MapLike<T> node = ops.getMap(item).getOrThrow();
            TrackNodeLocation location = TrackNodeLocation.decode(ops, node.get("Location"), dimensions);
            Vec3 normal = Vec3.CODEC.decode(ops, node.get("Normal")).getOrThrow().getFirst();
            graph.loadNode(location, nextNodeId(), normal);
            int index = i.getAndIncrement();
            nodes.put(index, node);
            indexTracker.put(index, graph.locateNode(location));
        });

        nodes.forEach((index, node) -> {
            TrackNode node1 = indexTracker.get(index);
            ops.getList(node.get("Connections")).result().ifPresent(connections -> connections.accept(item -> {
                MapLike<T> connection = ops.getMap(item).getOrThrow();
                TrackNode node2 = indexTracker.get(ops.getNumberValue(connection.get("To"), 0).intValue());
                TrackEdge edge = TrackEdge.decode(node1, node2, ops, connection.get("EdgeData"), graph, dimensions);
                graph.putConnection(node1, node2, edge);
            }));
        });

        return graph;
    }

}
