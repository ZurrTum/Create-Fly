package com.zurrtum.create.content.trains.entity;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.trains.graph.*;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TrainRelocator {
    public static boolean relocate(
        Train train,
        Level level,
        BlockPos pos,
        BezierTrackPointLocation bezier,
        boolean bezierDirection,
        Vec3 lookAngle,
        @Nullable List<Vec3> toVisualise
    ) {
        BlockState blockState = level.getBlockState(pos);
        if (!(blockState.getBlock() instanceof ITrackBlock track))
            return false;

        Pair<Vec3, Direction.AxisDirection> nearestTrackAxis = track.getNearestTrackAxis(level, pos, blockState, lookAngle);
        TrackGraphLocation graphLocation = bezier != null ? TrackGraphHelper.getBezierGraphLocationAt(
            level,
            pos,
            bezierDirection ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE,
            bezier
        ) : TrackGraphHelper.getGraphLocationAt(level, pos, nearestTrackAxis.getSecond(), nearestTrackAxis.getFirst());

        if (graphLocation == null)
            return false;

        TrackGraph graph = graphLocation.graph;
        TrackNode node1 = graph.locateNode(graphLocation.edge.getFirst());
        TrackNode node2 = graph.locateNode(graphLocation.edge.getSecond());
        TrackEdge edge = graph.getConnectionsFrom(node1).get(node2);
        if (edge == null)
            return false;

        TravellingPoint probe = new TravellingPoint(node1, node2, edge, graphLocation.position, false);
        TravellingPoint.IEdgePointListener ignoreSignals = probe.ignoreEdgePoints();
        TravellingPoint.ITurnListener ignoreTurns = probe.ignoreTurns();
        List<Pair<Couple<TrackNode>, Double>> recordedLocations = new ArrayList<>();
        List<Vec3> recordedVecs = new ArrayList<>();
        Consumer<TravellingPoint> recorder = tp -> {
            recordedLocations.add(Pair.of(Couple.create(tp.node1, tp.node2), tp.position));
            recordedVecs.add(tp.getPosition(graph));
        };
        TravellingPoint.ITrackSelector steer = probe.steer(TravellingPoint.SteerDirection.NONE, track.getUpNormal(level, pos, blockState));
        MutableBoolean blocked = new MutableBoolean(false);
        MutableBoolean portal = new MutableBoolean(false);

        MutableInt blockingIndex = new MutableInt(0);
        train.forEachTravellingPointBackwards((tp, d) -> {
            if (blocked.booleanValue())
                return;
            probe.travel(
                graph, d, steer, ignoreSignals, ignoreTurns, $ -> {
                    portal.setTrue();
                    return true;
                }
            );
            recorder.accept(probe);
            if (probe.blocked || portal.booleanValue()) {
                blocked.setTrue();
                return;
            }
            blockingIndex.increment();
        });

        if (level.isClientSide() && toVisualise != null && !recordedVecs.isEmpty()) {
            toVisualise.clear();
            toVisualise.add(recordedVecs.getFirst());
        }

        for (int i = 0; i < recordedVecs.size() - 1; i++) {
            Vec3 vec1 = recordedVecs.get(i);
            Vec3 vec2 = recordedVecs.get(i + 1);
            boolean blocking = i >= blockingIndex.intValue() - 1;
            boolean collided = !blocked.booleanValue() && train.findCollidingTrain(level, vec1, vec2, level.dimension()) != null;
            if (level.isClientSide() && toVisualise != null)
                toVisualise.add(vec2);
            if (collided || blocking)
                return false;
        }

        if (blocked.booleanValue())
            return false;

        if (toVisualise != null)
            return true;

        train.leaveStation();
        train.derailed = false;
        train.navigation.waitingForSignal = null;
        train.occupiedSignalBlocks.clear();
        train.graph = graph;
        train.speed = 0;
        train.migratingPoints.clear();
        train.cancelStall();

        if (train.navigation.destination != null)
            train.navigation.cancelNavigation();

        train.forEachTravellingPoint(tp -> {
            Pair<Couple<TrackNode>, Double> last = recordedLocations.removeLast();
            tp.node1 = last.getFirst().getFirst();
            tp.node2 = last.getFirst().getSecond();
            tp.position = last.getSecond();
            tp.edge = graph.getConnectionsFrom(tp.node1).get(tp.node2);
        });

        for (Carriage carriage : train.carriages)
            carriage.updateContraptionAnchors();

        train.status.successfulMigration();
        train.collectInitiallyOccupiedSignalBlocks();
        return true;
    }
}
