package com.zurrtum.create.client.content.logistics;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstancerProvider;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.transform.Translate;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.function.Consumer;

public class FlapStuffs {
    public static final int FLAP_COUNT = 4;
    public static final float X_OFFSET = 0.075f / 16.0f;
    public static final float SEGMENT_STEP = -3.05f / 16.0f;
    public static final Vec3 TUNNEL_PIVOT = VecHelper.voxelSpace(0, 10, 1.0f);
    public static final Vec3 FUNNEL_PIVOT = VecHelper.voxelSpace(0, 10, 9.5f);

    public static FlapsRenderState getFlapsRenderState(
        SuperByteBufferRenderState flapBuffer,
        Vec3 pivot,
        Direction funnelFacing,
        float flapness,
        float zOffset
    ) {
        float yRadians = Radians.horizontal(funnelFacing.getOpposite());
        float[] angles = new float[FLAP_COUNT];
        for (int segment = 0; segment < FLAP_COUNT; segment++) {
            angles[segment] = flapAngle(flapness, segment);
        }
        return new FlapsRenderState(flapBuffer, pivot, zOffset, yRadians, angles);
    }

    public static float flapAngle(float flapness, int segment) {
        float intensity = segment == 3 ? 1.5f : segment + 1;
        float abs = Math.abs(flapness);
        float flapAngle = Mth.sin((float) ((1 - abs) * Math.PI * intensity)) * 30 * flapness;
        if (flapness < 0) {
            flapAngle *= 0.5f;
        }
        return flapAngle;
    }

    public static Matrix4f commonTransform(BlockPos visualPosition, Direction side, float baseZOffset) {
        float yRadians = Radians.horizontal(side.getOpposite());

        return new Matrix4f().translate(visualPosition.getX(), visualPosition.getY(), visualPosition.getZ())
            .translate(Translate.CENTER, Translate.CENTER, Translate.CENTER).rotateY(yRadians)
            .translate(-Translate.CENTER, -Translate.CENTER, -Translate.CENTER).translate(X_OFFSET, 0, baseZOffset);
    }

    public static class Visual {
        private final TransformedInstance[] flaps;

        private final Matrix4f commonTransform = new Matrix4f();
        private final Vec3 pivot;

        public Visual(InstancerProvider instancerProvider, Matrix4fc commonTransform, Vec3 pivot, Model flapModel) {
            this.pivot = pivot;
            this.commonTransform.set(commonTransform).translate((float) pivot.x, (float) pivot.y, (float) pivot.z);

            flaps = new TransformedInstance[FLAP_COUNT];

            instancerProvider.instancer(InstanceTypes.TRANSFORMED, flapModel).createInstances(flaps);
        }

        public void update(float f) {
            for (int segment = 0; segment < FLAP_COUNT; segment++) {
                var flap = flaps[segment];

                flap.setTransform(commonTransform).rotateXDegrees(flapAngle(f, segment)).translateBack(pivot)
                    .translate(segment * SEGMENT_STEP, 0, 0).setChanged();
            }
        }

        public void delete() {
            for (TransformedInstance flap : flaps) {
                flap.delete();
            }
        }

        public void updateLight(int light) {
            for (TransformedInstance flap : flaps) {
                flap.light(light).setChanged();
            }
        }

        public void collectCrumblingInstances(Consumer<Instance> consumer) {
            for (TransformedInstance flap : flaps) {
                consumer.accept(flap);
            }
        }
    }

    public record FlapsRenderState(SuperByteBufferRenderState model, Vec3 pivot, float zOffset, float yRadians,
                                   float[] angles) {
        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            matrices.pushPose();
            Pose pose = matrices.last();
            if (yRadians != 0) {
                pose.rotateYCentered(yRadians);
            }
            pose.translate(X_OFFSET, 0, zOffset);
            float x = (float) pivot.x;
            float y = (float) pivot.y;
            float z = (float) pivot.z;
            for (int segment = 0; segment < FLAP_COUNT; segment++) {
                float angle = angles[segment];
                if (angle != 0) {
                    matrices.pushPose();
                    matrices.rotateXAround(Mth.DEG_TO_RAD * angle, x, y, z);
                    model.submit(matrices, queue);
                    matrices.popPose();
                } else {
                    model.submit(matrices, queue);
                }
                pose.translate(SEGMENT_STEP, 0, 0);
            }
            matrices.popPose();
        }
    }
}
