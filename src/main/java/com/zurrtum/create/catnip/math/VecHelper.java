package com.zurrtum.create.catnip.math;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public class VecHelper {
    public static final Vec3d CENTER_OF_ORIGIN = new Vec3d(.5, .5, .5);

    public static Vec3d rotate(Vec3d vec, Vec3d rotationVec) {
        return rotate(vec, rotationVec.x, rotationVec.y, rotationVec.z);
    }

    public static Vec3d rotate(Vec3d vec, double xRot, double yRot, double zRot) {
        return rotate(rotate(rotate(vec, xRot, Axis.X), yRot, Axis.Y), zRot, Axis.Z);
    }

    public static Vec3d rotateCentered(Vec3d vec, double deg, Axis axis) {
        Vec3d shift = getCenterOf(BlockPos.ZERO);
        return VecHelper.rotate(vec.subtract(shift), deg, axis).add(shift);
    }

    public static Vec3d rotate(Vec3d vec, double deg, Axis axis) {
        if (deg == 0)
            return vec;
        if (vec == Vec3d.ZERO)
            return vec;

        float angle = (float) (deg / 180f * Math.PI);
        double sin = MathHelper.sin(angle);
        double cos = MathHelper.cos(angle);
        double x = vec.x;
        double y = vec.y;
        double z = vec.z;

        if (axis == Axis.X)
            return new Vec3d(x, y * cos - z * sin, z * cos + y * sin);
        if (axis == Axis.Y)
            return new Vec3d(x * cos + z * sin, y, z * cos - x * sin);
        if (axis == Axis.Z)
            return new Vec3d(x * cos - y * sin, y * cos + x * sin, z);
        return vec;
    }

    public static Vec3d mirrorCentered(Vec3d vec, BlockMirror mirror) {
        Vec3d shift = getCenterOf(BlockPos.ZERO);
        return VecHelper.mirror(vec.subtract(shift), mirror).add(shift);
    }

    public static Vec3d mirror(Vec3d vec, BlockMirror mirror) {
        if (mirror == BlockMirror.NONE)
            return vec;
        if (vec == Vec3d.ZERO)
            return vec;

        double x = vec.x;
        double y = vec.y;
        double z = vec.z;

        if (mirror == BlockMirror.LEFT_RIGHT)
            return new Vec3d(x, y, -z);
        if (mirror == BlockMirror.FRONT_BACK)
            return new Vec3d(-x, y, z);
        return vec;
    }

    public static Vec3d lookAt(Vec3d vec, Vec3d fwd) {
        fwd = fwd.normalize();
        Vec3d up = new Vec3d(0, 1, 0);
        double dot = fwd.dotProduct(up);
        if (Math.abs(dot) > 1 - 1.0E-3)
            up = new Vec3d(0, 0, dot > 0 ? 1 : -1);
        Vec3d right = fwd.crossProduct(up).normalize();
        up = right.crossProduct(fwd).normalize();
        double x = vec.x * right.x + vec.y * up.x + vec.z * fwd.x;
        double y = vec.x * right.y + vec.y * up.y + vec.z * fwd.y;
        double z = vec.x * right.z + vec.y * up.z + vec.z * fwd.z;
        return new Vec3d(x, y, z);
    }

    public static boolean isVecPointingTowards(Vec3d vec, Direction direction) {
        return Vec3d.of(direction.getVector()).dotProduct(vec.normalize()) > 0.125; // slight tolerance to activate perpendicular movement actors
    }

    public static Vec3d getCenterOf(Vec3i pos) {
        if (pos.equals(Vec3i.ZERO))
            return CENTER_OF_ORIGIN;
        return Vec3d.of(pos).add(.5f, .5f, .5f);
    }

    public static Vec3d offsetRandomly(Vec3d vec, Random r, float radius) {
        return new Vec3d(
            vec.x + (r.nextFloat() - .5f) * 2 * radius,
            vec.y + (r.nextFloat() - .5f) * 2 * radius,
            vec.z + (r.nextFloat() - .5f) * 2 * radius
        );
    }

    public static Vec3d axisAlingedPlaneOf(Vec3d vec) {
        vec = vec.normalize();
        return new Vec3d(1, 1, 1).subtract(Math.abs(vec.x), Math.abs(vec.y), Math.abs(vec.z));
    }

    public static Vec3d axisAlingedPlaneOf(Direction face) {
        return axisAlingedPlaneOf(Vec3d.of(face.getVector()));
    }

    public static NbtList writeNBT(Vec3d vec) {
        NbtList listnbt = new NbtList();
        listnbt.add(NbtDouble.of(vec.x));
        listnbt.add(NbtDouble.of(vec.y));
        listnbt.add(NbtDouble.of(vec.z));
        return listnbt;
    }

    public static NbtCompound writeNBTCompound(Vec3d vec) {
        NbtCompound compoundTag = new NbtCompound();
        compoundTag.put("V", writeNBT(vec));
        return compoundTag;
    }

    public static Vec3d readNBT(NbtList list) {
        if (list.isEmpty())
            return Vec3d.ZERO;
        return new Vec3d(list.getDouble(0, 0), list.getDouble(1, 0), list.getDouble(2, 0));
    }

    public static Vec3d readNBTCompound(NbtCompound nbt) {
        return readNBT(nbt.getListOrEmpty("V"));
    }

    public static void write(Vec3d vec, PacketByteBuf buffer) {
        buffer.writeDouble(vec.x);
        buffer.writeDouble(vec.y);
        buffer.writeDouble(vec.z);
    }

    public static Vec3d read(PacketByteBuf buffer) {
        return new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public static Vec3d voxelSpace(double x, double y, double z) {
        return new Vec3d(x, y, z).multiply(1 / 16f);
    }

    public static int getCoordinate(Vec3i pos, Axis axis) {
        return axis.choose(pos.getX(), pos.getY(), pos.getZ());
    }

    public static float getCoordinate(Vec3d vec, Axis axis) {
        return (float) axis.choose(vec.x, vec.y, vec.z);
    }

    public static boolean onSameAxis(BlockPos pos1, BlockPos pos2, Axis axis) {
        if (pos1.equals(pos2))
            return true;
        for (Axis otherAxis : Axis.values())
            if (axis != otherAxis)
                if (getCoordinate(pos1, otherAxis) != getCoordinate(pos2, otherAxis))
                    return false;
        return true;
    }

    public static Vec3d clamp(Vec3d vec, float maxLength) {
        return vec.lengthSquared() > maxLength * maxLength ? vec.normalize().multiply(maxLength) : vec;
    }

    public static Vec3d lerp(float p, Vec3d from, Vec3d to) {
        return from.add(to.subtract(from).multiply(p));
    }

    public static Vec3d slerp(float p, Vec3d from, Vec3d to) {
        double theta = Math.acos(from.dotProduct(to));
        return from.multiply(MathHelper.sin(1 - p) * theta).add(to.multiply(MathHelper.sin((float) (theta * p))))
            .multiply(1 / MathHelper.sin((float) theta));
    }

    public static Vec3d clampComponentWise(Vec3d vec, float maxLength) {
        return new Vec3d(
            MathHelper.clamp(vec.x, -maxLength, maxLength),
            MathHelper.clamp(vec.y, -maxLength, maxLength),
            MathHelper.clamp(vec.z, -maxLength, maxLength)
        );
    }

    public static Vec3d componentMin(Vec3d vec1, Vec3d vec2) {
        return new Vec3d(Math.min(vec1.x, vec2.x), Math.min(vec1.y, vec2.y), Math.min(vec1.z, vec2.z));
    }

    public static Vec3d componentMax(Vec3d vec1, Vec3d vec2) {
        return new Vec3d(Math.max(vec1.x, vec2.x), Math.max(vec1.y, vec2.y), Math.max(vec1.z, vec2.z));
    }

    public static Vec3d project(Vec3d vec, Vec3d ontoVec) {
        if (ontoVec.equals(Vec3d.ZERO))
            return Vec3d.ZERO;
        return ontoVec.multiply(vec.dotProduct(ontoVec) / ontoVec.lengthSquared());
    }

    @Nullable
    public static Vec3d intersectSphere(Vec3d origin, Vec3d lineDirection, Vec3d sphereCenter, double radius) {
        if (lineDirection.equals(Vec3d.ZERO))
            return null;
        if (lineDirection.lengthSquared() != 1)
            lineDirection = lineDirection.normalize();

        Vec3d diff = origin.subtract(sphereCenter);
        double lineDotDiff = lineDirection.dotProduct(diff);
        double delta = lineDotDiff * lineDotDiff - (diff.lengthSquared() - radius * radius);
        if (delta < 0)
            return null;
        double t = -lineDotDiff + Math.sqrt(delta);
        return origin.add(lineDirection.multiply(t));
    }

    public static Vec3d bezier(Vec3d p1, Vec3d p2, Vec3d q1, Vec3d q2, float t) {
        Vec3d v1 = lerp(t, p1, q1);
        Vec3d v2 = lerp(t, q1, q2);
        Vec3d v3 = lerp(t, q2, p2);
        Vec3d inner1 = lerp(t, v1, v2);
        Vec3d inner2 = lerp(t, v2, v3);
        return lerp(t, inner1, inner2);
    }

    public static Vec3d bezierDerivative(Vec3d p1, Vec3d p2, Vec3d q1, Vec3d q2, float t) {
        return p1.multiply(-3 * t * t + 6 * t - 3).add(q1.multiply(9 * t * t - 12 * t + 3)).add(q2.multiply(-9 * t * t + 6 * t))
            .add(p2.multiply(3 * t * t));
    }

    public static double @Nullable [] intersectRanged(Vec3d p1, Vec3d q1, Vec3d p2, Vec3d q2, Axis plane) {
        Vec3d pDiff = p2.subtract(p1);
        Vec3d qDiff = q2.subtract(q1);
        double[] intersect = intersect(p1, q1, pDiff.normalize(), qDiff.normalize(), plane);
        if (intersect == null)
            return null;
        if (intersect[0] < 0 || intersect[1] < 0)
            return null;
        if (intersect[0] * intersect[0] > pDiff.lengthSquared() || intersect[1] * intersect[1] > qDiff.lengthSquared())
            return null;
        return intersect;
    }

    public static double @Nullable [] intersect(Vec3d p1, Vec3d p2, Vec3d r, Vec3d s, Axis plane) {
        if (plane == Axis.X) {
            p1 = new Vec3d(p1.y, 0, p1.z);
            p2 = new Vec3d(p2.y, 0, p2.z);
            r = new Vec3d(r.y, 0, r.z);
            s = new Vec3d(s.y, 0, s.z);
        }

        if (plane == Axis.Z) {
            p1 = new Vec3d(p1.x, 0, p1.y);
            p2 = new Vec3d(p2.x, 0, p2.y);
            r = new Vec3d(r.x, 0, r.y);
            s = new Vec3d(s.x, 0, s.y);
        }

        Vec3d qminusp = p2.subtract(p1);
        double rcs = r.x * s.z - r.z * s.x;
        if (MathHelper.approximatelyEquals(rcs, 0))
            return null;
        Vec3d rdivrcs = r.multiply(1 / rcs);
        Vec3d sdivrcs = s.multiply(1 / rcs);
        double t = qminusp.x * sdivrcs.z - qminusp.z * sdivrcs.x;
        double u = qminusp.x * rdivrcs.z - qminusp.z * rdivrcs.x;
        return new double[]{t, u};
    }

    public static double alignedDistanceToFace(Vec3d pos, BlockPos blockPos, Direction face) {
        Axis axis = face.getAxis();
        return Math.abs(getCoordinate(
            pos,
            axis
        ) - (blockPos.getComponentAlongAxis(axis) + (face.getDirection() == Direction.AxisDirection.POSITIVE ? 1 : 0)));
    }
}
