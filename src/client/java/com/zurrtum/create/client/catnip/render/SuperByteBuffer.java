package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.lib.transform.Transform;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedTransferQueue;

@SuppressWarnings("UnusedReturnValue")
public class SuperByteBuffer implements Transform<SuperByteBuffer> {
    private static final LinkedTransferQueue<SuperByteBufferTask> queue = new LinkedTransferQueue<>();
    private static final ConcurrentLinkedQueue<SuperByteBufferTask> pool = new ConcurrentLinkedQueue<>();
    private EntityBlockTemplateMesh @UnknownNullability [] templates;
    private @UnknownNullability SuperByteBufferTask task;

    protected SuperByteBuffer() {
    }

    public SuperByteBuffer(EntityBlockTemplateMesh[] templates) {
        this.templates = templates;
        task = pool.poll();
        if (task == null) {
            task = new SuperByteBufferTask();
        }
    }

    public static SuperByteBuffer empty() {
        return EmptySuperByteBuffer.INSTANCE;
    }

    public static void nudge(Pose pose, int seed) {
        long randomBits = seed * 31L * 493286711L;
        randomBits = randomBits * randomBits * 4392167121L + randomBits * 98761L;
        float xNudge = (((randomBits >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float yNudge = (((randomBits >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float zNudge = (((randomBits >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        pose.translate(xNudge, yNudge, zNudge);
    }

    public static void mul(Pose pose, Pose transform) {
        pose.pose().mul(transform.pose());
        if (pose.trustedNormals && transform.trustedNormals) {
            pose.normal().mul(transform.normal());
        } else {
            pose.computeNormalMatrix();
        }
    }

    public static void scaleAround(Pose pose, float sx, float sy, float sz, float ox, float oy, float oz) {
        pose.pose().scaleAround(sx, sy, sz, ox, oy, oz);
        if (Math.abs(sx) == Math.abs(sy) && Math.abs(sy) == Math.abs(sz)) {
            if (sx < 0.0F || sy < 0.0F || sz < 0.0F) {
                pose.normal().scale(Math.signum(sx), Math.signum(sy), Math.signum(sz));
            }
        } else {
            pose.normal().scale(1.0F / sx, 1.0F / sy, 1.0F / sz);
            pose.trustedNormals = false;
        }
    }

    public static void scaleAround(Pose pose, float factor, float ox, float oy, float oz) {
        pose.pose().scaleAround(factor, factor, factor, ox, oy, oz);
        if (factor < 0.0F) {
            float signum = Math.signum(factor);
            pose.normal().scale(signum, signum, signum);
        }
    }

    public static void copyTransform(SuperByteBuffer from, SuperByteBuffer to) {
        if (from.isEmpty()) {
            return;
        }
        mul(to.task.pose, from.task.pose);
    }

    public SuperByteBufferRenderState extractRenderState() {
        if ((task.flag & 0b11111) != 0) {
            SuperByteBufferRenderState state = task.submit(queue, pool, templates);
            task = pool.poll();
            if (task == null) {
                task = new SuperByteBufferTask();
            }
            return state;
        }
        return task.resolve(templates);
    }

    public void submit(PoseStack matrices, OrderedSubmitNodeCollector queue) {
        extractRenderState().submit(matrices, queue);
    }

    public void submit(RenderType type, PoseStack matrices, OrderedSubmitNodeCollector queue) {
        extractRenderState().submit(type, matrices, queue);
    }

    @Deprecated
    public void renderInto(Pose pose, VertexConsumer consumer) {
        extractRenderState().renderInto(pose, consumer);
    }

    public SuperByteBuffer reset() {
        task.reset();
        return this;
    }

    public boolean isEmpty() {
        return false;
    }

    public SuperByteBuffer cardinalLighting(@Nullable Level level) {
        return cardinalLighting(level instanceof BlockAndTintGetter getter ? getter.cardinalLighting() : null);
    }

    public SuperByteBuffer cardinalLighting(@Nullable CardinalLighting light) {
        if (light == CardinalLighting.DEFAULT) {
            task.flag |= 0b010000000;
        } else if (light == CardinalLighting.NETHER) {
            task.flag |= (byte) 0b100000000;
        } else {
            task.flag &= 0b001111111;
        }
        return this;
    }

    @Override
    public SuperByteBuffer scale(float factorX, float factorY, float factorZ) {
        task.pose.scale(factorX, factorY, factorZ);
        return this;
    }

    @Override
    public SuperByteBuffer rotate(Quaternionfc quaternion) {
        Pose pose = task.pose;
        pose.pose().rotate(quaternion);
        pose.normal().rotate(quaternion);
        return this;
    }

    @Override
    public SuperByteBuffer translate(float x, float y, float z) {
        task.pose.translate(x, y, z);
        return this;
    }

    @Override
    public SuperByteBuffer transform(Pose pose) {
        mul(task.pose, pose);
        return this;
    }

    @Override
    public SuperByteBuffer mulPose(Matrix4fc pose) {
        task.pose.pose().mul(pose);
        return this;
    }

    @Override
    public SuperByteBuffer mulNormal(Matrix3fc normal) {
        task.pose.normal().mul(normal);
        return this;
    }

    @Override
    public SuperByteBuffer rotateCentered(float radians, float axisX, float axisY, float axisZ) {
        if (radians != 0) {
            task.pose.rotateCentered(axisX, axisY, axisZ, radians);
        }
        return this;
    }

    @Override
    public SuperByteBuffer rotateCentered(float radians, Axis axis) {
        if (radians != 0) {
            task.pose.rotateCentered(axis, radians);
        }
        return this;
    }

    @Override
    public SuperByteBuffer rotateCentered(float radians, Direction.Axis axis) {
        if (radians != 0) {
            task.pose.rotateCentered(axis, radians);
        }
        return this;
    }

    @Override
    public SuperByteBuffer rotateCentered(float radians, Direction direction) {
        if (radians != 0) {
            task.pose.rotateCentered(direction, radians);
        }
        return this;
    }

    @Override
    public SuperByteBuffer rotateXCentered(float radians) {
        if (radians != 0) {
            task.pose.rotateXCentered(radians);
        }
        return this;
    }

    @Override
    public SuperByteBuffer rotateYCentered(float radians) {
        if (radians != 0) {
            task.pose.rotateYCentered(radians);
        }
        return this;
    }

    @Override
    public SuperByteBuffer rotateZCentered(float radians) {
        if (radians != 0) {
            task.pose.rotateZCentered(radians);
        }
        return this;
    }

    public SuperByteBuffer rotateAround(
        float radians,
        float axisX,
        float axisY,
        float axisZ,
        float pivotX,
        float pivotY,
        float pivotZ
    ) {
        if (radians != 0) {
            task.pose.rotateAround(axisX, axisY, axisZ, radians, pivotX, pivotY, pivotZ);
        }
        return this;
    }

    public SuperByteBuffer rotateAround(float radians, Axis axis, float pivotX, float pivotY, float pivotZ) {
        if (radians != 0) {
            task.pose.rotateAround(axis, radians, pivotX, pivotY, pivotZ);
        }
        return this;
    }

    public SuperByteBuffer rotateAround(float radians, Direction.Axis axis, float pivotX, float pivotY, float pivotZ) {
        if (radians != 0) {
            task.pose.rotateAround(axis, radians, pivotX, pivotY, pivotZ);
        }
        return this;
    }

    public SuperByteBuffer rotateAround(float radians, Direction direction, float pivotX, float pivotY, float pivotZ) {
        if (radians != 0) {
            task.pose.rotateAround(direction, radians, pivotX, pivotY, pivotZ);
        }
        return this;
    }

    public SuperByteBuffer rotateXAround(float radians, float pivotX, float pivotY, float pivotZ) {
        if (radians != 0) {
            task.pose.rotateXAround(radians, pivotX, pivotY, pivotZ);
        }
        return this;
    }

    public SuperByteBuffer rotateYAround(float radians, float pivotX, float pivotY, float pivotZ) {
        if (radians != 0) {
            task.pose.rotateYAround(radians, pivotX, pivotY, pivotZ);
        }
        return this;
    }

    public SuperByteBuffer rotateZAround(float radians, float pivotX, float pivotY, float pivotZ) {
        if (radians != 0) {
            task.pose.rotateZAround(radians, pivotX, pivotY, pivotZ);
        }
        return this;
    }

    @Override
    public SuperByteBuffer rotate(float radians, float axisX, float axisY, float axisZ) {
        if (radians != 0) {
            task.pose.rotate(axisX, axisY, axisZ, radians);
        }
        return this;
    }

    @Override
    public SuperByteBuffer rotate(float radians, Axis axis) {
        if (radians != 0) {
            task.pose.rotate(axis, radians);
        }
        return this;
    }

    @Override
    public SuperByteBuffer rotate(float radians, Direction direction) {
        if (radians != 0) {
            task.pose.rotate(direction, radians);
        }
        return this;
    }

    @Override
    public SuperByteBuffer rotate(float radians, Direction.Axis axis) {
        if (radians != 0) {
            task.pose.rotate(axis, radians);
        }
        return this;
    }

    @Override
    public SuperByteBuffer rotateX(float radians) {
        if (radians != 0) {
            task.pose.rotate(Axis.XP, radians);
        }
        return this;
    }

    @Override
    public SuperByteBuffer rotateY(float radians) {
        if (radians != 0) {
            task.pose.rotate(Axis.YP, radians);
        }
        return this;
    }

    @Override
    public SuperByteBuffer rotateZ(float radians) {
        if (radians != 0) {
            task.pose.rotate(Axis.ZP, radians);
        }
        return this;
    }

    @Override
    public SuperByteBuffer rotateToFace(Direction facing) {
        switch (facing) {
            case DOWN -> task.pose.rotate(Axis.XP, Radians.DEG_270);
            case UP -> task.pose.rotate(Axis.XP, Radians.DEG_90);
            case SOUTH -> task.pose.rotate(Axis.YP, Radians.DEG_180);
            case WEST -> task.pose.rotate(Axis.YP, Radians.DEG_90);
            case EAST -> task.pose.rotate(Axis.YP, Radians.DEG_270);
        }
        return this;
    }

    public SuperByteBuffer color(int r, int g, int b, int a) {
        task.color = ARGB.color(a, r, g, b);
        if (task.color != -1) {
            task.flag |= 1;
        }
        return this;
    }

    public SuperByteBuffer color(int color) {
        if (color != -1) {
            task.color = color;
            task.flag |= 1;
        }
        return this;
    }

    public SuperByteBuffer color(Color c) {
        return color(c.getRGB() | 0xFF000000);
    }

    public SuperByteBuffer disableDiffuse() {
        task.flag |= 0b100000;
        return this;
    }

    public SuperByteBuffer keepAlive() {
        task.flag |= 0b1000000;
        return this;
    }

    public SuperByteBuffer shiftUV(SpriteShiftEntry entry) {
        task.flag |= 0b100;
        task.shiftEntry = entry;
        return this;
    }

    public SuperByteBuffer shiftUVScrolling(SpriteShiftEntry entry, float scrollV) {
        task.flag |= 0b010;
        task.shiftEntry = entry;
        task.shiftU = 0;
        task.shiftV = scrollV;
        return this;
    }

    public SuperByteBuffer shiftUVScrolling(SpriteShiftEntry entry, float scrollU, float scrollV) {
        task.flag |= 0b010;
        task.shiftEntry = entry;
        task.shiftU = scrollU;
        task.shiftV = scrollV;
        return this;
    }

    public SuperByteBuffer shiftUVtoSheet(SpriteShiftEntry entry, float scrollU, float scrollV, int sheetSize) {
        task.flag |= 0b110;
        task.shiftEntry = entry;
        task.shiftU = scrollU;
        task.shiftV = scrollV;
        task.sheetSize = sheetSize;
        return this;
    }

    public SuperByteBuffer overlay(int overlay) {
        task.overlay = overlay;
        return this;
    }

    public SuperByteBuffer light(int packedLight) {
        if (packedLight != 0) {
            task.flag |= 0b01000;
            task.packedLight = packedLight;
        }
        return this;
    }

    public SuperByteBuffer useLevelLight(BlockAndLightGetter level) {
        task.flag |= 0b10000;
        task.blockAndLightGetter = level;
        if (level instanceof BlockAndTintGetter blockAndTintGetter) {
            cardinalLighting(blockAndTintGetter.cardinalLighting());
        }
        return this;
    }

    public SuperByteBuffer useLevelLight(BlockAndLightGetter level, Matrix4f lightTransform) {
        task.flag |= 0b10000;
        task.blockAndLightGetter = level;
        task.lightTransform = lightTransform;
        if (level instanceof BlockAndTintGetter blockAndTintGetter) {
            cardinalLighting(blockAndTintGetter.cardinalLighting());
        }
        return this;
    }

    public static void register() {
        int thread = Runtime.getRuntime().availableProcessors();
        boolean priority = thread > 4;
        int count = Math.max(thread - 2, 1);
        for (int i = 0; i < count; i++) {
            Thread worker = new SuperByteBufferThread(i, queue, pool);
            if (priority) {
                worker.setPriority(Thread.NORM_PRIORITY + 1);
            }
            worker.setDaemon(true);
            worker.start();
        }
    }
}
