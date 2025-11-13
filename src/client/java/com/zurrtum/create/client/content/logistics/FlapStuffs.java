package com.zurrtum.create.client.content.logistics;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstancerProvider;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.transform.Translate;
import net.minecraft.client.renderer.rendertype.RenderType;
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
    public static final float X_OFFSET = 0.075f / 16f;
    public static final float SEGMENT_STEP = -3.05f / 16f;
    public static final Vec3 TUNNEL_PIVOT = VecHelper.voxelSpace(0, 10, 1f);
    public static final Vec3 FUNNEL_PIVOT = VecHelper.voxelSpace(0, 10, 9.5f);

    public static FlapsRenderState getFlapsRenderState(
        SuperByteBuffer flapBuffer,
        Vec3 pivot,
        Direction funnelFacing,
        float flapness,
        float zOffset,
        int light
    ) {
        float horizontalAngle = Mth.DEG_TO_RAD * AngleHelper.horizontalAngle(funnelFacing.getOpposite());
        float[] angles = new float[FLAP_COUNT];
        for (int segment = 0; segment < FLAP_COUNT; segment++) {
            angles[segment] = Mth.DEG_TO_RAD * flapAngle(flapness, segment);
        }
        PoseStack.Pose[] entries = new PoseStack.Pose[FLAP_COUNT];
        return new FlapsRenderState(flapBuffer, pivot, zOffset, light, horizontalAngle, angles, entries);
    }

    public static float flapAngle(float flapness, int segment) {
        float intensity = segment == 3 ? 1.5f : segment + 1;
        float abs = Math.abs(flapness);
        float flapAngle = Mth.sin((float) ((1 - abs) * Math.PI * intensity)) * 30 * flapness;
        if (flapness < 0)
            flapAngle *= .5f;
        return flapAngle;
    }

    public static Matrix4f commonTransform(BlockPos visualPosition, Direction side, float baseZOffset) {
        float horizontalAngle = AngleHelper.horizontalAngle(side.getOpposite());

        return new Matrix4f().translate(visualPosition.getX(), visualPosition.getY(), visualPosition.getZ())
            .translate(Translate.CENTER, Translate.CENTER, Translate.CENTER).rotateY(Mth.DEG_TO_RAD * horizontalAngle)
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

                flap.setTransform(commonTransform).rotateXDegrees(flapAngle(f, segment)).translateBack(pivot).translate(segment * SEGMENT_STEP, 0, 0)
                    .setChanged();
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

    public record FlapsRenderState(
        SuperByteBuffer model, Vec3 pivot, float zOffset, int light, float horizontalAngle, float[] angles, PoseStack.Pose[] entries
    ) implements SubmitNodeCollector.CustomGeometryRenderer {
        public void render(RenderType layer, PoseStack matrices, SubmitNodeCollector queue) {
            matrices.pushPose();
            matrices.translate(0.5f, 0.5f, 0.5f);
            matrices.mulPose(Axis.YP.rotation(horizontalAngle));
            matrices.translate(-0.5f, -0.5f, -0.5f);
            matrices.translate(X_OFFSET, 0, zOffset);
            for (int segment = 0; segment < FLAP_COUNT; segment++) {
                matrices.pushPose();
                matrices.translate(pivot.x, pivot.y, pivot.z);
                matrices.mulPose(Axis.XP.rotation(angles[segment]));
                matrices.translate(-pivot.x, -pivot.y, -pivot.z);
                entries[segment] = matrices.last().copy();
                matrices.popPose();
                matrices.translate(SEGMENT_STEP, 0, 0);
            }
            matrices.popPose();
            queue.submitCustomGeometry(matrices, layer, this);
        }

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            for (int segment = 0; segment < FLAP_COUNT; segment++) {
                model.light(light).renderInto(entries[segment], vertexConsumer);
            }
        }
    }
}
