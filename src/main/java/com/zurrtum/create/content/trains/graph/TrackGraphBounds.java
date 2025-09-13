package com.zurrtum.create.content.trains.graph;

import com.zurrtum.create.content.trains.track.BezierConnection;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrackGraphBounds {

    public Box box;
    public List<BezierConnection> beziers;

    public TrackGraphBounds(TrackGraph graph, RegistryKey<World> dimension) {
        beziers = new ArrayList<>();
        box = null;

        for (TrackNode node : graph.nodes.values()) {
            if (node.location.dimension != dimension)
                continue;
            include(node);
            Map<TrackNode, TrackEdge> connections = graph.getConnectionsFrom(node);
            for (TrackEdge edge : connections.values())
                if (edge.turn != null && edge.turn.isPrimary())
                    beziers.add(edge.turn);
        }

        if (box != null)
            box = box.expand(2);
    }

    private void include(TrackNode node) {
        Vec3d v = node.location.getLocation();
        Box aabb = new Box(v, v);
        box = box == null ? aabb : box.union(aabb);
    }

}
