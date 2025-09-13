package com.zurrtum.create.client.content.kinetics.chainConveyor;

import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.content.trains.track.TrackBlockOutline;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.jetbrains.annotations.Nullable;

public abstract class ChainConveyorShape {

    @Nullable
    public abstract Vec3d intersect(Vec3d from, Vec3d to);

    public abstract float getChainPosition(Vec3d intersection);

    protected abstract void drawOutline(BlockPos anchor, MatrixStack ms, VertexConsumer vb);

    public abstract Vec3d getVec(BlockPos anchor, float position);

    public static class ChainConveyorOBB extends ChainConveyorShape {

        public BlockPos connection;
        double yaw, pitch;
        Box bounds;
        Vec3d pivot;
        final double radius = 0.175;
        VoxelShape voxelShape;

        Vec3d[] linePoints;

        public ChainConveyorOBB(BlockPos connection, Vec3d start, Vec3d end) {
            this.connection = connection;
            Vec3d diff = end.subtract(start);
            double d = diff.length();
            double dxz = diff.multiply(1, 0, 1).length();
            yaw = MathHelper.DEGREES_PER_RADIAN * MathHelper.atan2(diff.x, diff.z);
            pitch = MathHelper.DEGREES_PER_RADIAN * MathHelper.atan2(-diff.y, dxz);
            bounds = new Box(start, start).stretch(0, 0, d).expand(radius, radius, 0);
            pivot = start;
            voxelShape = VoxelShapes.cuboid(bounds);
        }

        @Override
        public Vec3d intersect(Vec3d from, Vec3d to) {
            from = counterTransform(from);
            to = counterTransform(to);

            Vec3d result = bounds.raycast(from, to).orElse(null);
            if (result == null)
                return null;

            result = transform(result);
            return result;
        }

        private Vec3d counterTransform(Vec3d from) {
            from = from.subtract(pivot);
            from = VecHelper.rotate(from, -yaw, Axis.Y);
            from = VecHelper.rotate(from, -pitch, Axis.X);
            from = from.add(pivot);
            return from;
        }

        private Vec3d transform(Vec3d result) {
            result = result.subtract(pivot);
            result = VecHelper.rotate(result, pitch, Axis.X);
            result = VecHelper.rotate(result, yaw, Axis.Y);
            result = result.add(pivot);
            return result;
        }

        @Override
        public void drawOutline(BlockPos anchor, MatrixStack ms, VertexConsumer vb) {
            TransformStack.of(ms).translate(pivot).rotateYDegrees((float) yaw).rotateXDegrees((float) pitch).translateBack(pivot);
            TrackBlockOutline.renderShape(voxelShape, ms, vb, null);
        }

        @Override
        public float getChainPosition(Vec3d intersection) {
            int dots = (int) Math.round(Vec3d.of(connection).length() - 3);
            double length = bounds.getLengthZ();
            double selection = Math.min(bounds.getLengthZ(), intersection.distanceTo(pivot));

            double margin = length - dots;
            selection = MathHelper.clamp(selection - margin, 0, length - margin * 2);
            selection = Math.round(selection);

            return (float) (selection + margin + 0.025);
        }

        @Override
        public Vec3d getVec(BlockPos anchor, float position) {
            float x = (float) bounds.getCenter().x;
            float y = (float) bounds.getCenter().y;
            Vec3d from = new Vec3d(x, y, bounds.minZ);
            Vec3d to = new Vec3d(x, y, bounds.maxZ);
            Vec3d point = from.lerp(to, MathHelper.clamp(position / from.distanceTo(to), 0, 1));
            point = transform(point);
            return point.add(Vec3d.of(anchor));
        }
    }

    public static class ChainConveyorBB extends ChainConveyorShape {

        Vec3d lb, rb;
        final double radius = 0.875;
        Box bounds;

        public ChainConveyorBB(Vec3d center) {
            lb = center.add(0, 0, 0);
            rb = center.add(0, 0.5, 0);
            bounds = new Box(lb, rb).expand(1, 0, 1);
        }

        @Override
        public Vec3d intersect(Vec3d from, Vec3d to) {
            return bounds.raycast(from, to).orElse(null);
        }

        @Override
        public void drawOutline(BlockPos anchor, MatrixStack ms, VertexConsumer vb) {
            TrackBlockOutline.renderShape(AllShapes.CHAIN_CONVEYOR_INTERACTION, ms, vb, null);
        }

        @Override
        public float getChainPosition(Vec3d intersection) {
            Vec3d diff = bounds.getCenter().subtract(intersection);
            float angle = (float) (MathHelper.DEGREES_PER_RADIAN * MathHelper.atan2(diff.x, diff.z) + 360 + 180) % 360;
            float rounded = Math.round(angle / 45) * 45f;
            return rounded;
        }

        @Override
        public Vec3d getVec(BlockPos anchor, float position) {
            Vec3d point = bounds.getCenter();
            point = point.add(VecHelper.rotate(new Vec3d(0, 0, radius), position, Axis.Y));
            return point.add(Vec3d.of(anchor)).add(0, -.125, 0);
        }

    }

}
