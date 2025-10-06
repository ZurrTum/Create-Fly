package com.zurrtum.create.client.content.trains.graph;

import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllKeys;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.content.trains.graph.*;
import com.zurrtum.create.content.trains.signal.SignalBoundary;
import com.zurrtum.create.content.trains.signal.SignalEdgeGroup;
import com.zurrtum.create.content.trains.signal.TrackEdgePoint;
import com.zurrtum.create.content.trains.track.BezierConnection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class TrackGraphVisualizer {

    public static void visualiseSignalEdgeGroups(MinecraftClient mc, TrackGraph graph) {
        Entity cameraEntity = mc.getCameraEntity();
        if (cameraEntity == null)
            return;
        Box box = graph.getBounds(mc.world).box;
        if (box == null || !box.intersects(cameraEntity.getBoundingBox().expand(50)))
            return;

        Vec3d camera = cameraEntity.getEyePos();
        Outliner outliner = Outliner.getInstance();
        Map<UUID, SignalEdgeGroup> allGroups = Create.RAILWAYS.signalEdgeGroups;
        float width = 1 / 8f;

        for (Map.Entry<TrackNodeLocation, TrackNode> nodeEntry : graph.nodes.entrySet()) {
            TrackNodeLocation nodeLocation = nodeEntry.getKey();
            TrackNode node = nodeEntry.getValue();
            if (nodeLocation == null)
                continue;

            Vec3d location = nodeLocation.getLocation();
            if (location.distanceTo(camera) > 50)
                continue;
            if (!mc.world.getRegistryKey().equals(nodeLocation.dimension))
                continue;

            Map<TrackNode, TrackEdge> map = graph.connectionsByNode.get(node);
            if (map == null)
                continue;

            int hashCode = node.hashCode();
            for (Map.Entry<TrackNode, TrackEdge> entry : map.entrySet()) {
                TrackNode other = entry.getKey();
                TrackEdge edge = entry.getValue();
                EdgeData signalData = edge.getEdgeData();

                if (!edge.node1.getLocation().dimension.equals(edge.node2.getLocation().dimension))
                    continue;
                if (other.hashCode() > hashCode && other.getLocation().getLocation().distanceTo(camera) <= 50)
                    continue;

                Vec3d yOffset = new Vec3d(0, (other.hashCode() > hashCode ? 6 : 5) / 64f, 0);
                Vec3d startPoint = edge.getPosition(graph, 0);
                Vec3d endPoint = edge.getPosition(graph, 1);

                if (!edge.isTurn()) {

                    // Straight edge with signal boundaries
                    if (signalData.hasSignalBoundaries()) {
                        double prev = 0;
                        double length = edge.getLength();
                        SignalBoundary prevBoundary = null;
                        SignalEdgeGroup group = null;

                        for (TrackEdgePoint trackEdgePoint : signalData.getPoints()) {
                            if (!(trackEdgePoint instanceof SignalBoundary boundary))
                                continue;

                            prevBoundary = boundary;
                            group = allGroups.get(boundary.getGroup(node));

                            if (group != null)
                                outliner.showLine(
                                    Pair.of(boundary, edge),
                                    edge.getPosition(graph, prev + (prev == 0 ? 0 : 1 / 16f / length)).add(yOffset),
                                    edge.getPosition(graph, (prev = boundary.getLocationOn(edge) / length) - 1 / 16f / length).add(yOffset)
                                ).colored(group.color.get()).lineWidth(width);

                        }

                        if (prevBoundary != null) {
                            group = allGroups.get(prevBoundary.getGroup(other));
                            if (group != null)
                                outliner.showLine(edge, edge.getPosition(graph, prev + 1 / 16f / length).add(yOffset), endPoint.add(yOffset))
                                    .colored(group.color.get()).lineWidth(width);
                            continue;
                        }
                    }

                    // Straight edge, no signal boundaries
                    UUID singleGroup = signalData.getEffectiveEdgeGroupId(graph);
                    SignalEdgeGroup singleEdgeGroup = singleGroup == null ? null : allGroups.get(singleGroup);
                    if (singleEdgeGroup == null)
                        continue;
                    outliner.showLine(edge, startPoint.add(yOffset), endPoint.add(yOffset)).colored(singleEdgeGroup.color.get()).lineWidth(width);

                } else {

                    // Bezier edge with signal boundaries
                    if (signalData.hasSignalBoundaries()) {
                        Iterator<TrackEdgePoint> points = signalData.getPoints().iterator();
                        SignalBoundary currentBoundary = null;
                        double currentBoundaryPosition = 0;
                        while (points.hasNext()) {
                            TrackEdgePoint next = points.next();
                            if (!(next instanceof SignalBoundary signal))
                                continue;
                            currentBoundary = signal;
                            currentBoundaryPosition = signal.getLocationOn(edge);
                            break;
                        }

                        if (currentBoundary == null)
                            continue;
                        UUID initialGroupId = currentBoundary.getGroup(node);
                        if (initialGroupId == null)
                            continue;
                        SignalEdgeGroup initialGroup = allGroups.get(initialGroupId);
                        if (initialGroup == null)
                            continue;

                        Color currentColour = initialGroup.color.get();
                        Vec3d previous = null;
                        BezierConnection turn = edge.getTurn();

                        for (int i = 0; i <= turn.getSegmentCount(); i++) {
                            double f = i * 1f / turn.getSegmentCount();
                            double position = f * turn.getLength();
                            Vec3d current = edge.getPosition(graph, f);

                            if (previous != null) {
                                if (currentBoundary != null && position > currentBoundaryPosition) {
                                    current = edge.getPosition(graph, (currentBoundaryPosition - width) / turn.getLength());
                                    outliner.showLine(Pair.of(edge, previous), previous.add(yOffset), current.add(yOffset)).colored(currentColour)
                                        .lineWidth(width);
                                    current = edge.getPosition(graph, (currentBoundaryPosition + width) / turn.getLength());
                                    previous = current;
                                    UUID newId = currentBoundary.getGroup(other);
                                    if (newId != null && allGroups.containsKey(newId))
                                        currentColour = allGroups.get(newId).color.get();

                                    currentBoundary = null;
                                    while (points.hasNext()) {
                                        TrackEdgePoint next = points.next();
                                        if (!(next instanceof SignalBoundary signal))
                                            continue;
                                        currentBoundary = signal;
                                        currentBoundaryPosition = signal.getLocationOn(edge);
                                        break;
                                    }
                                }

                                outliner.showLine(Pair.of(edge, previous), previous.add(yOffset), current.add(yOffset)).colored(currentColour)
                                    .lineWidth(width);
                            }

                            previous = current;
                        }
                    }

                    // Bezier edge, no signal boundaries
                    UUID singleGroup = signalData.getEffectiveEdgeGroupId(graph);
                    SignalEdgeGroup singleEdgeGroup = singleGroup == null ? null : allGroups.get(singleGroup);
                    if (singleEdgeGroup == null)
                        continue;
                    Vec3d previous = null;
                    BezierConnection turn = edge.getTurn();
                    for (int i = 0; i <= turn.getSegmentCount(); i++) {
                        Vec3d current = edge.getPosition(graph, i * 1f / turn.getSegmentCount());
                        if (previous != null)
                            outliner.showLine(Pair.of(edge, previous), previous.add(yOffset), current.add(yOffset))
                                .colored(singleEdgeGroup.color.get()).lineWidth(width);
                        previous = current;
                    }
                }
            }
        }
    }

    public static void debugViewGraph(MinecraftClient mc, TrackGraph graph, boolean extended) {
        Entity cameraEntity = mc.getCameraEntity();
        if (cameraEntity == null)
            return;
        Box box = graph.getBounds(mc.world).box;
        if (box == null || !box.intersects(cameraEntity.getBoundingBox().expand(50)))
            return;

        Vec3d camera = cameraEntity.getEyePos();
        for (Map.Entry<TrackNodeLocation, TrackNode> nodeEntry : graph.nodes.entrySet()) {
            TrackNodeLocation nodeLocation = nodeEntry.getKey();
            TrackNode node = nodeEntry.getValue();
            if (nodeLocation == null)
                continue;

            Vec3d location = nodeLocation.getLocation();
            if (location.distanceTo(camera) > 50)
                continue;
            if (!mc.world.getRegistryKey().equals(nodeLocation.dimension))
                continue;

            Vec3d yOffset = new Vec3d(0, 3 / 16f, 0);
            Vec3d v1 = location.add(yOffset);
            Vec3d v2 = v1.add(node.getNormal().multiply(3 / 16f));
            Outliner.getInstance().showLine(Integer.valueOf(node.getNetId()), v1, v2).colored(Color.mixColors(Color.WHITE, graph.color, 1))
                .lineWidth(1 / 8f);

            Map<TrackNode, TrackEdge> map = graph.connectionsByNode.get(node);
            if (map == null)
                continue;

            int hashCode = node.hashCode();
            for (Map.Entry<TrackNode, TrackEdge> entry : map.entrySet()) {
                TrackNode other = entry.getKey();
                TrackEdge edge = entry.getValue();

                if (!edge.node1.getLocation().dimension.equals(edge.node2.getLocation().dimension)) {
                    v1 = location.add(yOffset);
                    v2 = v1.add(node.getNormal().multiply(3 / 16f));
                    Outliner.getInstance().showLine(Integer.valueOf(node.getNetId()), v1, v2).colored(Color.mixColors(Color.WHITE, graph.color, 1))
                        .lineWidth(1 / 4f);
                    continue;
                }
                if (other.hashCode() > hashCode && !AllKeys.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL))
                    continue;

                yOffset = new Vec3d(0, (other.hashCode() > hashCode ? 6 : 4) / 16f, 0);
                if (!edge.isTurn()) {
                    if (extended) {
                        Vec3d materialPos = edge.getPosition(graph, 0.5).add(0, 1, 0);
                        Outliner.getInstance().showItem(Pair.of(edge, edge.edgeData), materialPos, new ItemStack(edge.getTrackMaterial()));
                        Outliner.getInstance().showAABB(edge.edgeData, Box.of(materialPos, .25, 0, .25).offset(0, -0.5, 0)).lineWidth(1 / 16f)
                            .colored(graph.color);
                    }
                    Outliner.getInstance().showLine(edge, edge.getPosition(graph, 0).add(yOffset), edge.getPosition(graph, 1).add(yOffset))
                        .colored(graph.color).lineWidth(1 / 16f);
                    continue;
                }

                Vec3d previous = null;
                BezierConnection turn = edge.getTurn();
                if (extended) {
                    Vec3d materialPos = edge.getPosition(graph, 0.5).add(0, 1, 0);
                    Outliner.getInstance().showItem(Pair.of(edge, edge.edgeData), materialPos, new ItemStack(edge.getTrackMaterial()));
                    Outliner.getInstance().showAABB(edge.edgeData, Box.of(materialPos, .25, 0, .25).offset(0, -0.5, 0)).lineWidth(1 / 16f)
                        .colored(graph.color);
                }
                for (int i = 0; i <= turn.getSegmentCount(); i++) {
                    Vec3d current = edge.getPosition(graph, i * 1f / turn.getSegmentCount());
                    if (previous != null)
                        Outliner.getInstance().showLine(Pair.of(edge, previous), previous.add(yOffset), current.add(yOffset)).colored(graph.color)
                            .lineWidth(1 / 16f);
                    previous = current;
                }
            }
        }
    }

}