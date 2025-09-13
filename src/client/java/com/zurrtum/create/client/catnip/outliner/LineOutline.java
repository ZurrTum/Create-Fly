package com.zurrtum.create.client.catnip.outliner;

import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import org.joml.Vector4f;

public class LineOutline extends Outline {

    protected final Vector3d start = new Vector3d(0, 0, 0);
    protected final Vector3d end = new Vector3d(0, 0, 0);

    public LineOutline set(Vector3d start, Vector3d end) {
        this.start.set(start.x, start.y, start.z);
        this.end.set(end.x, end.y, end.z);
        return this;
    }

    public LineOutline set(Vec3d start, Vec3d end) {
        this.start.set(start.x, start.y, start.z);
        this.end.set(end.x, end.y, end.z);
        return this;
    }

    @Override
    public void render(MinecraftClient mc, MatrixStack ms, SuperRenderTypeBuffer buffer, Vec3d camera, float pt) {
        float width = params.getLineWidth();
        if (width == 0)
            return;

        VertexConsumer consumer = buffer.getBuffer(PonderRenderTypes.outlineSolid());
        params.loadColor(colorTemp);
        Vector4f color = colorTemp;
        int lightmap = params.lightmap;
        boolean disableLineNormals = params.disableLineNormals;
        renderInner(ms, consumer, camera, pt, width, color, lightmap, disableLineNormals);
    }

    protected void renderInner(
        MatrixStack ms,
        VertexConsumer consumer,
        Vec3d camera,
        float pt,
        float width,
        Vector4f color,
        int lightmap,
        boolean disableNormals
    ) {
        bufferCuboidLine(ms, consumer, camera, start, end, width, color, lightmap, disableNormals);
    }

    public static class EndChasingLineOutline extends LineOutline {
        private float progress = 0;
        private float prevProgress = 0;
        private boolean lockStart;

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
        protected void renderInner(
            MatrixStack ms,
            VertexConsumer consumer,
            Vec3d camera,
            float pt,
            float width,
            Vector4f color,
            int lightmap,
            boolean disableNormals
        ) {
            float distanceToTarget = MathHelper.lerp(pt, prevProgress, progress);

            Vector3d end;
            if (lockStart) {
                end = this.start;
            } else {
                end = this.end;
                distanceToTarget = 1 - distanceToTarget;
            }

            Vector3d start = this.startTemp;
            double x = (this.start.x - end.x) * distanceToTarget + end.x;
            double y = (this.start.y - end.y) * distanceToTarget + end.y;
            double z = (this.start.z - end.z) * distanceToTarget + end.z;
            start.set((float) x, (float) y, (float) z);
            bufferCuboidLine(ms, consumer, camera, start, end, width, color, lightmap, disableNormals);
        }
    }

}
