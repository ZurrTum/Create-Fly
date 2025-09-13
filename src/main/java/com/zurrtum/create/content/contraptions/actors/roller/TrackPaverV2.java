package com.zurrtum.create.content.contraptions.actors.roller;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.trains.graph.TrackEdge;
import com.zurrtum.create.content.trains.graph.TrackGraph;
import com.zurrtum.create.content.trains.track.BezierConnection;
import net.minecraft.util.math.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TrackPaverV2 {

    public static void pave(PaveTask task, TrackGraph graph, TrackEdge edge, double from, double to) {
        if (edge.isTurn()) {
            paveCurve(task, edge.getTurn(), from, to);
            return;
        }

        Vec3d location1 = edge.node1.getLocation().getLocation();
        Vec3d location2 = edge.node2.getLocation().getLocation();
        Vec3d diff = location2.subtract(location1);
        Vec3d direction = VecHelper.clampComponentWise(diff, 1);
        int extent = (int) Math.round((to - from) / direction.length());
        double length = edge.getLength();

        BlockPos pos = BlockPos.ofFloored(edge.getPosition(graph, MathHelper.clamp(from, 1 / 16f, length - 1 / 16f) / length)
            .subtract(0, diff.y != 0 ? 1 : 0.5, 0));

        paveStraight(task, pos, direction, extent);
    }

    public static void paveStraight(PaveTask task, BlockPos startPos, Vec3d direction, int extent) {
        Set<BlockPos> toPlaceOn = new HashSet<>();
        Vec3d start = VecHelper.getCenterOf(startPos);
        Vec3d mainNormal = direction.crossProduct(new Vec3d(0, 1, 0));
        Vec3d normalizedDirection = direction.normalize();

        boolean isDiagonalTrack = direction.multiply(1, 0, 1).length() > 1.125f;
        double r1 = task.getHorizontalInterval().getFirst();
        int flip = (int) Math.signum(r1);
        double r2 = r1 + flip;

        if (isDiagonalTrack) {
            r1 /= MathHelper.SQUARE_ROOT_OF_TWO;
            r2 /= MathHelper.SQUARE_ROOT_OF_TWO;
        }

        int currentOffset = (int) (Math.abs(r1) * 2 + .5f);
        int nextOffset = (int) (Math.abs(r2) * 2 + .5f);

        for (int i = 0; i < extent; i++) {
            Vec3d offset = direction.multiply(i);
            Vec3d mainPos = start.add(offset.x, offset.y, offset.z);
            Vec3d targetVec = mainPos.add(mainNormal.multiply(flip * (int) (currentOffset / 2.0)));

            if (!isDiagonalTrack) {
                toPlaceOn.add(BlockPos.ofFloored(targetVec));
                continue;
            }

            boolean placeRow = currentOffset % 2 == 0 || nextOffset % 2 == 1;
            boolean placeSides = currentOffset % 2 == 1 || nextOffset % 2 == 0;

            if (placeSides) {
                for (int side : Iterate.positiveAndNegative) {
                    Vec3d sideOffset = normalizedDirection.multiply(side).add(mainNormal.normalize().multiply(flip)).multiply(.5);
                    toPlaceOn.add(BlockPos.ofFloored(targetVec.add(sideOffset)));
                }
            }

            if (placeRow) {
                if (Math.abs(currentOffset % 2) == 1)
                    targetVec = mainPos.add(mainNormal.multiply(flip * (int) ((currentOffset + 1) / 2.0)));
                toPlaceOn.add(BlockPos.ofFloored(targetVec));
            }

        }

        toPlaceOn.forEach(task::put);
    }

    public static void paveCurve(PaveTask task, BezierConnection bc, double from, double to) {
        Map<Pair<Integer, Integer>, Double> yLevels = new HashMap<>();
        Map<Pair<Integer, Integer>, Double> tLevels = new HashMap<>();

        BlockPos bePosition = bc.bePositions.getFirst();
        double radius = -task.getHorizontalInterval().getFirst();
        double r1 = radius - .575;
        double r2 = radius + .575;

        double handleLength = bc.getHandleLength();
        Vec3d start = bc.starts.getFirst().subtract(Vec3d.of(bePosition)).add(0, 3 / 16f, 0);
        Vec3d end = bc.starts.getSecond().subtract(Vec3d.of(bePosition)).add(0, 3 / 16f, 0);
        Vec3d startHandle = bc.axes.getFirst().multiply(handleLength).add(start);
        Vec3d endHandle = bc.axes.getSecond().multiply(handleLength).add(end);
        Vec3d startNormal = bc.normals.getFirst();
        Vec3d endNormal = bc.normals.getSecond();

        int segCount = bc.getSegmentCount();
        float[] lut = bc.getStepLUT();
        double localFrom = from / bc.getLength();
        double localTo = to / bc.getLength();

        for (int i = 0; i < segCount; i++) {

            float t = i == segCount ? 1 : i * lut[i] / segCount;
            float t1 = (i + 1) == segCount ? 1 : (i + 1) * lut[(i + 1)] / segCount;

            if (t1 < localFrom)
                continue;
            if (t > localTo)
                continue;

            Vec3d vt = VecHelper.bezier(start, end, startHandle, endHandle, t);
            Vec3d vNormal = startNormal.equals(endNormal) ? startNormal : VecHelper.slerp(t, startNormal, endNormal);
            Vec3d hNormal = vNormal.crossProduct(VecHelper.bezierDerivative(start, end, startHandle, endHandle, t).normalize()).normalize();
            vt = vt.add(vNormal.multiply(-1.175f));

            Vec3d vt1 = VecHelper.bezier(start, end, startHandle, endHandle, t1);
            Vec3d vNormal1 = startNormal.equals(endNormal) ? startNormal : VecHelper.slerp(t1, startNormal, endNormal);
            Vec3d hNormal1 = vNormal1.crossProduct(VecHelper.bezierDerivative(start, end, startHandle, endHandle, t1).normalize()).normalize();
            vt1 = vt1.add(vNormal1.multiply(-1.175f));

            Vec3d a3 = vt.add(hNormal.multiply(r2));
            Vec3d b3 = vt1.add(hNormal1.multiply(r2));
            Vec3d c3 = vt1.add(hNormal1.multiply(r1));
            Vec3d d3 = vt.add(hNormal.multiply(r1));

            Vec2f a = vec2(a3);
            Vec2f b = vec2(b3);
            Vec2f c = vec2(c3);
            Vec2f d = vec2(d3);

            Box aabb = new Box(a3, b3).union(new Box(c3, d3));

            double y = vt.add(vt1).y / 2f;
            for (int scanX = MathHelper.floor(aabb.minX); scanX <= aabb.maxX; scanX++) {
                for (int scanZ = MathHelper.floor(aabb.minZ); scanZ <= aabb.maxZ; scanZ++) {

                    Vec2f p = new Vec2f(scanX + .5f, scanZ + .5f);
                    if (!isInTriangle(a, b, c, p) && !isInTriangle(a, c, d, p))
                        continue;

                    Pair<Integer, Integer> key = Pair.of(scanX, scanZ);
                    if (!yLevels.containsKey(key) || yLevels.get(key) > y) {
                        yLevels.put(key, y);
                        tLevels.put(key, (t + t1) / 2d);
                    }
                }
            }

        }

        //

        for (Map.Entry<Pair<Integer, Integer>, Double> entry : yLevels.entrySet()) {
            double yValue = entry.getValue();
            int floor = MathHelper.floor(yValue);
            BlockPos targetPos = new BlockPos(entry.getKey().getFirst(), floor, entry.getKey().getSecond()).add(bePosition);
            task.put(targetPos.getX(), targetPos.getZ(), targetPos.getY() + (yValue - floor >= .5 ? .5f : 0));
        }
    }

    private static Vec2f vec2(Vec3d vec3) {
        return new Vec2f((float) vec3.x, (float) vec3.z);
    }

    private static boolean isInTriangle(Vec2f a, Vec2f b, Vec2f c, Vec2f p) {
        float pcx = p.x - c.x;
        float pcy = p.y - c.y;
        float cbx = c.x - b.x;
        float bcy = b.y - c.y;
        float d = bcy * (a.x - c.x) + cbx * (a.y - c.y);
        float s = bcy * pcx + cbx * pcy;
        float t = (c.y - a.y) * pcx + (a.x - c.x) * pcy;
        return d < 0 ? s <= 0 && t <= 0 && s + t >= d : s >= 0 && t >= 0 && s + t <= d;
    }

    public static double lineToPointDiff2d(Vec3d l1, Vec3d l2, Vec3d p) {
        return Math.abs((l2.x - l1.x) * (l1.z - p.z) - (l1.x - p.x) * (l2.z - l1.z));
    }

}