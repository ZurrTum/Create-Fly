package com.zurrtum.create.content.trains.entity;

import com.mojang.serialization.*;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.trains.graph.*;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class TrainMigration {

    Couple<TrackNodeLocation> locations;
    double positionOnOldEdge;
    boolean curve;
    Vec3 fallback;

    public TrainMigration() {
    }

    public TrainMigration(TravellingPoint point) {
        double t = point.position / point.edge.getLength();
        fallback = point.edge.getPosition(null, t);
        curve = point.edge.isTurn();
        positionOnOldEdge = point.position;
        locations = Couple.create(point.node1.getLocation(), point.node2.getLocation());
    }

    public TrackGraphLocation tryMigratingTo(TrackGraph graph) {
        TrackNode node1 = graph.locateNode(locations.getFirst());
        TrackNode node2 = graph.locateNode(locations.getSecond());
        if (node1 != null && node2 != null) {
            TrackEdge edge = graph.getConnectionsFrom(node1).get(node2);
            if (edge != null) {
                TrackGraphLocation graphLocation = new TrackGraphLocation();
                graphLocation.graph = graph;
                graphLocation.edge = locations;
                graphLocation.position = positionOnOldEdge;
                return graphLocation;
            }
        }

        if (curve)
            return null;

        Vec3 prevDirection = locations.getSecond().getLocation().subtract(locations.getFirst().getLocation()).normalize();

        for (TrackNodeLocation loc : graph.getNodes()) {
            Vec3 nodeVec = loc.getLocation();
            if (nodeVec.distanceToSqr(fallback) > 32 * 32)
                continue;

            TrackNode newNode1 = graph.locateNode(loc);
            for (Map.Entry<TrackNode, TrackEdge> entry : graph.getConnectionsFrom(newNode1).entrySet()) {
                TrackEdge edge = entry.getValue();
                if (edge.isTurn())
                    continue;
                TrackNode newNode2 = entry.getKey();
                float radius = 1 / 64f;
                Vec3 direction = edge.getDirection(true);
                if (!Mth.equal(direction.dot(prevDirection), 1))
                    continue;
                Vec3 intersectSphere = VecHelper.intersectSphere(nodeVec, direction, fallback, radius);
                if (intersectSphere == null)
                    continue;
                if (!Mth.equal(direction.dot(intersectSphere.subtract(nodeVec).normalize()), 1))
                    continue;
                double edgeLength = edge.getLength();
                double position = intersectSphere.distanceTo(nodeVec) - radius;
                if (Double.isNaN(position))
                    continue;
                if (position < 0)
                    continue;
                if (position > edgeLength)
                    continue;

                TrackGraphLocation graphLocation = new TrackGraphLocation();
                graphLocation.graph = graph;
                graphLocation.edge = Couple.create(loc, newNode2.getLocation());
                graphLocation.position = position;
                return graphLocation;
            }
        }

        return null;
    }

    public void write(ValueOutput view, DimensionPalette dimensions) {
        view.putBoolean("Curve", curve);
        view.store("Fallback", Vec3.CODEC, fallback);
        view.putDouble("Position", positionOnOldEdge);
        ValueOutput.ValueOutputList list = view.childrenList("Nodes");
        locations.getFirst().write(list.addChild(), dimensions);
        locations.getSecond().write(list.addChild(), dimensions);
    }

    public static <T> DataResult<T> encode(final TrainMigration input, final DynamicOps<T> ops, final T empty, DimensionPalette dimensions) {
        RecordBuilder<T> map = ops.mapBuilder();
        map.add("Curve", ops.createBoolean(input.curve));
        map.add("Fallback", input.fallback, Vec3.CODEC);
        map.add("Curve", ops.createDouble(input.positionOnOldEdge));
        ListBuilder<T> list = ops.listBuilder();
        list.add(TrackNodeLocation.encode(input.locations.getFirst(), ops, empty, dimensions));
        list.add(TrackNodeLocation.encode(input.locations.getSecond(), ops, empty, dimensions));
        map.add("Nodes", list.build(empty));
        return map.build(empty);
    }

    public static TrainMigration read(ValueInput view, DimensionPalette dimensions) {
        TrainMigration trainMigration = new TrainMigration();
        trainMigration.curve = view.getBooleanOr("Curve", false);
        trainMigration.fallback = view.read("Fallback", Vec3.CODEC).orElse(Vec3.ZERO);
        trainMigration.positionOnOldEdge = view.getDoubleOr("Position", 0);
        Iterator<ValueInput> iterator = view.childrenListOrEmpty("Nodes").iterator();
        trainMigration.locations = Couple.create(
            TrackNodeLocation.read(iterator.next(), dimensions),
            TrackNodeLocation.read(iterator.next(), dimensions)
        );
        return trainMigration;
    }

    public static <T> TrainMigration decode(DynamicOps<T> ops, T input, DimensionPalette dimensions) {
        MapLike<T> map = ops.getMap(input).getOrThrow();
        TrainMigration trainMigration = new TrainMigration();
        trainMigration.curve = ops.getBooleanValue(map.get("Curve")).result().orElse(false);
        trainMigration.fallback = Vec3.CODEC.parse(ops, map.get("Fallback")).result().orElse(Vec3.ZERO);
        trainMigration.positionOnOldEdge = ops.getNumberValue(map.get("Position"), 0).doubleValue();
        Iterator<T> iterator = ops.getStream(map.get("Nodes")).getOrThrow().iterator();
        trainMigration.locations = Couple.create(
            TrackNodeLocation.decode(ops, iterator.next(), dimensions),
            TrackNodeLocation.decode(ops, iterator.next(), dimensions)
        );
        return trainMigration;
    }

}
