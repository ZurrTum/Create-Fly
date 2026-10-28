package com.zurrtum.create.client.catnip.outliner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector.CustomGeometryRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class LineOutline extends Outline {
    protected final Vector3f diffPosTemp = new Vector3f();
    protected final Vector3d start = new Vector3d(0, 0, 0);
    protected final Vector3d end = new Vector3d(0, 0, 0);

    public LineOutline set(Vector3d start, Vector3d end) {
        this.start.set(start.x, start.y, start.z);
        this.end.set(end.x, end.y, end.z);
        return this;
    }

    public LineOutline set(Vec3 start, Vec3 end) {
        this.start.set(start.x, start.y, start.z);
        this.end.set(end.x, end.y, end.z);
        return this;
    }

    @Override
    public void submit(Minecraft mc, PoseStack ms, SubmitNodeCollector queue, Vec3 camera, float pt) {
        float width = params.getLineWidth();
        if (width != 0) {
            submitInner(
                ms,
                queue,
                camera,
                pt,
                start,
                end,
                width,
                params.color,
                params.lightmap,
                params.disableLineNormals
            );
        }
    }

    protected void submitInner(
        PoseStack ms,
        SubmitNodeCollector queue,
        Vec3 camera,
        float pt,
        Vector3d start,
        Vector3d end,
        float width,
        int color,
        int lightmap,
        boolean disableNormals
    ) {
        ms.pushPose();
        Pose pose = ms.last();
        Vector3f diff = diffPosTemp.set(
            (float) (end.x - start.x),
            (float) (end.y - start.y),
            (float) (end.z - start.z)
        );
        pose.translate((float) (start.x - camera.x), (float) (start.y - camera.y), (float) (start.z - camera.z));
        double yRadians = Mth.atan2(diff.x(), diff.z());
        if (yRadians != 0) {
            pose.rotate(Axis.YP, (float) yRadians);
        }
        double xRadians = Mth.atan2(Mth.sqrt(diff.x() * diff.x() + diff.z() * diff.z()), diff.y()) - Mth.HALF_PI;
        pose.rotate(Axis.XP, (float) xRadians);
        float length = Mth.sqrt(diff.x() * diff.x() + diff.y() * diff.y() + diff.z() * diff.z());
        LineRenderState state = new LineRenderState(this, length, width, color, lightmap, disableNormals);
        queue.submitCustomGeometry(ms, PonderRenderTypes.outlineSolid(), state);
        ms.popPose();
    }

    public static class EndChasingLineOutline extends LineOutline {
        private float progress;
        private float prevProgress;
        private final boolean lockStart;

        private final Vector3d startTemp = new Vector3d(0, 0, 0);

        public EndChasingLineOutline(boolean lockStart) {
            this.lockStart = lockStart;
        }

        public EndChasingLineOutline setProgress(float progress) {
            prevProgress = this.progress;
            this.progress = progress;
            return this;
        }

        @Override
        protected void submitInner(
            PoseStack ms,
            SubmitNodeCollector queue,
            Vec3 camera,
            float pt,
            Vector3d start,
            Vector3d end,
            float width,
            int color,
            int lightmap,
            boolean disableNormals
        ) {
            if (lockStart) {
                end = start;
            } else {
                float distanceToTarget = 1 - Mth.lerp(pt, prevProgress, progress);
                start = startTemp.set(
                    (start.x - end.x) * distanceToTarget + end.x,
                    (start.y - end.y) * distanceToTarget + end.y,
                    (start.z - end.z) * distanceToTarget + end.z
                );
            }
            super.submitInner(ms, queue, camera, pt, start, end, width, color, lightmap, disableNormals);
        }
    }

    protected record LineRenderState(LineOutline outline, float length, float width, int color, int lightmap,
                                     boolean disableNormals) implements CustomGeometryRenderer {
        protected static final Vector3f ORIGIN = new Vector3f();

        @Override
        public void render(Pose pose, VertexConsumer buffer) {
            outline.bufferCuboidLine(
                pose,
                buffer,
                ORIGIN,
                Direction.SOUTH,
                length,
                width,
                color,
                lightmap,
                disableNormals
            );
        }
    }
}
