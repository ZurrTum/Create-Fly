package com.zurrtum.create.client.catnip.outliner;

import com.zurrtum.create.client.catnip.render.BindableTexture;
import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class AABBOutline extends Outline {

    protected Box bb;

    protected final Vector3f minPosTemp1 = new Vector3f();
    protected final Vector3f maxPosTemp1 = new Vector3f();

    protected final Vector4f colorTemp1 = new Vector4f();
    protected final Vector3f pos0Temp = new Vector3f();
    protected final Vector3f pos1Temp = new Vector3f();
    protected final Vector3f pos2Temp = new Vector3f();
    protected final Vector3f pos3Temp = new Vector3f();
    protected final Vector3f normalTemp = new Vector3f();
    protected final Vector3f originTemp = new Vector3f();

    public AABBOutline(Box bb) {
        setBounds(bb);
    }

    public Box getBounds() {
        return bb;
    }

    public void setBounds(Box bb) {
        this.bb = bb;
    }

    @Override
    public void render(MinecraftClient mc, MatrixStack ms, SuperRenderTypeBuffer buffer, Vec3d camera, float pt) {
        params.loadColor(colorTemp);
        Vector4f color = colorTemp;
        int lightmap = params.lightmap;
        boolean disableLineNormals = params.disableLineNormals;
        renderBox(ms, buffer, camera, bb, color, lightmap, disableLineNormals);
    }

    protected void renderBox(
        MatrixStack ms,
        SuperRenderTypeBuffer buffer,
        Vec3d camera,
        Box box,
        Vector4f color,
        int lightmap,
        boolean disableLineNormals
    ) {
        Vector3f minPos = minPosTemp1;
        Vector3f maxPos = maxPosTemp1;

        boolean cameraInside = box.contains(camera);
        boolean cull = !cameraInside && !params.disableCull;
        float inflate = cameraInside ? -1 / 128f : 1 / 128f;

        box = box.offset(camera.multiply(-1));
        minPos.set((float) box.minX - inflate, (float) box.minY - inflate, (float) box.minZ - inflate);
        maxPos.set((float) box.maxX + inflate, (float) box.maxY + inflate, (float) box.maxZ + inflate);

        renderBoxFaces(ms, buffer, cull, params.getHighlightedFace(), minPos, maxPos, color, lightmap);

        float lineWidth = params.getLineWidth();
        if (lineWidth == 0)
            return;

        VertexConsumer consumer = buffer.getBuffer(PonderRenderTypes.outlineSolid());
        renderBoxEdges(ms, consumer, minPos, maxPos, lineWidth, color, lightmap, disableLineNormals);
    }

    protected void renderBoxFaces(
        MatrixStack ms,
        SuperRenderTypeBuffer buffer,
        boolean cull,
        Direction highlightedFace,
        Vector3f minPos,
        Vector3f maxPos,
        Vector4f color,
        int lightmap
    ) {
        MatrixStack.Entry pose = ms.peek();
        renderBoxFace(pose, buffer, cull, highlightedFace, minPos, maxPos, Direction.DOWN, color, lightmap);
        renderBoxFace(pose, buffer, cull, highlightedFace, minPos, maxPos, Direction.UP, color, lightmap);
        renderBoxFace(pose, buffer, cull, highlightedFace, minPos, maxPos, Direction.NORTH, color, lightmap);
        renderBoxFace(pose, buffer, cull, highlightedFace, minPos, maxPos, Direction.SOUTH, color, lightmap);
        renderBoxFace(pose, buffer, cull, highlightedFace, minPos, maxPos, Direction.WEST, color, lightmap);
        renderBoxFace(pose, buffer, cull, highlightedFace, minPos, maxPos, Direction.EAST, color, lightmap);
    }

    protected void renderBoxFace(
        MatrixStack.Entry pose,
        SuperRenderTypeBuffer buffer,
        boolean cull,
        Direction highlightedFace,
        Vector3f minPos,
        Vector3f maxPos,
        Direction face,
        Vector4f color,
        int lightmap
    ) {
        boolean highlighted = face == highlightedFace;

        // TODO: Presumably, the other texture should be used, but this was not noticed before so fixing it may lead to suboptimal visuals.
        //BindableTexture faceTexture = highlighted ? params.hightlightedFaceTexture : params.faceTexture;
        BindableTexture faceTexture = params.faceTexture;
        if (faceTexture == null)
            return;

        RenderLayer renderType = PonderRenderTypes.outlineTranslucent(faceTexture.getLocation(), cull);
        VertexConsumer consumer = buffer.getLateBuffer(renderType);

        float alphaMult = highlighted ? 1 : 0.5f;
        colorTemp1.set(color.x(), color.y(), color.z(), color.w() * alphaMult);
        color = colorTemp1;

        renderBoxFace(pose, consumer, minPos, maxPos, face, color, lightmap);
    }

    protected void renderBoxFace(
        MatrixStack.Entry pose,
        VertexConsumer consumer,
        Vector3f minPos,
        Vector3f maxPos,
        Direction face,
        Vector4f color,
        int lightmap
    ) {
        Vector3f pos0 = pos0Temp;
        Vector3f pos1 = pos1Temp;
        Vector3f pos2 = pos2Temp;
        Vector3f pos3 = pos3Temp;
        Vector3f normal = normalTemp;

        float minX = minPos.x();
        float minY = minPos.y();
        float minZ = minPos.z();
        float maxX = maxPos.x();
        float maxY = maxPos.y();
        float maxZ = maxPos.z();

        float maxU;
        float maxV;

        switch (face) {
            case DOWN -> {
                // 0 1 2 3
                pos0.set(minX, minY, maxZ);
                pos1.set(minX, minY, minZ);
                pos2.set(maxX, minY, minZ);
                pos3.set(maxX, minY, maxZ);
                maxU = maxX - minX;
                maxV = maxZ - minZ;
                normal.set(0, -1, 0);
            }
            case UP -> {
                // 4 5 6 7
                pos0.set(minX, maxY, minZ);
                pos1.set(minX, maxY, maxZ);
                pos2.set(maxX, maxY, maxZ);
                pos3.set(maxX, maxY, minZ);
                maxU = maxX - minX;
                maxV = maxZ - minZ;
                normal.set(0, 1, 0);
            }
            case NORTH -> {
                // 7 2 1 4
                pos0.set(maxX, maxY, minZ);
                pos1.set(maxX, minY, minZ);
                pos2.set(minX, minY, minZ);
                pos3.set(minX, maxY, minZ);
                maxU = maxX - minX;
                maxV = maxY - minY;
                normal.set(0, 0, -1);
            }
            case SOUTH -> {
                // 5 0 3 6
                pos0.set(minX, maxY, maxZ);
                pos1.set(minX, minY, maxZ);
                pos2.set(maxX, minY, maxZ);
                pos3.set(maxX, maxY, maxZ);
                maxU = maxX - minX;
                maxV = maxY - minY;
                normal.set(0, 0, 1);
            }
            case WEST -> {
                // 4 1 0 5
                pos0.set(minX, maxY, minZ);
                pos1.set(minX, minY, minZ);
                pos2.set(minX, minY, maxZ);
                pos3.set(minX, maxY, maxZ);
                maxU = maxZ - minZ;
                maxV = maxY - minY;
                normal.set(-1, 0, 0);
            }
            case EAST -> {
                // 6 3 2 7
                pos0.set(maxX, maxY, maxZ);
                pos1.set(maxX, minY, maxZ);
                pos2.set(maxX, minY, minZ);
                pos3.set(maxX, maxY, minZ);
                maxU = maxZ - minZ;
                maxV = maxY - minY;
                normal.set(1, 0, 0);
            }
            default -> {
                maxU = 1;
                maxV = 1;
            }
        }

        bufferQuad(pose, consumer, pos0, pos1, pos2, pos3, color, 0, 0, maxU, maxV, lightmap, normal);
    }

    protected void renderBoxEdges(
        MatrixStack ms,
        VertexConsumer consumer,
        Vector3f minPos,
        Vector3f maxPos,
        float lineWidth,
        Vector4f color,
        int lightmap,
        boolean disableNormals
    ) {
        Vector3f origin = originTemp;

        MatrixStack.Entry pose = ms.peek();

        float lineLengthX = maxPos.x() - minPos.x();
        float lineLengthY = maxPos.y() - minPos.y();
        float lineLengthZ = maxPos.z() - minPos.z();

        origin.set(minPos);
        bufferCuboidLine(pose, consumer, origin, Direction.EAST, lineLengthX, lineWidth, color, lightmap, disableNormals);
        bufferCuboidLine(pose, consumer, origin, Direction.UP, lineLengthY, lineWidth, color, lightmap, disableNormals);
        bufferCuboidLine(pose, consumer, origin, Direction.SOUTH, lineLengthZ, lineWidth, color, lightmap, disableNormals);

        origin.set(maxPos.x(), minPos.y(), minPos.z());
        bufferCuboidLine(pose, consumer, origin, Direction.UP, lineLengthY, lineWidth, color, lightmap, disableNormals);
        bufferCuboidLine(pose, consumer, origin, Direction.SOUTH, lineLengthZ, lineWidth, color, lightmap, disableNormals);

        origin.set(minPos.x(), maxPos.y(), minPos.z());
        bufferCuboidLine(pose, consumer, origin, Direction.EAST, lineLengthX, lineWidth, color, lightmap, disableNormals);
        bufferCuboidLine(pose, consumer, origin, Direction.SOUTH, lineLengthZ, lineWidth, color, lightmap, disableNormals);

        origin.set(minPos.x(), minPos.y(), maxPos.z());
        bufferCuboidLine(pose, consumer, origin, Direction.EAST, lineLengthX, lineWidth, color, lightmap, disableNormals);
        bufferCuboidLine(pose, consumer, origin, Direction.UP, lineLengthY, lineWidth, color, lightmap, disableNormals);

        origin.set(minPos.x(), maxPos.y(), maxPos.z());
        bufferCuboidLine(pose, consumer, origin, Direction.EAST, lineLengthX, lineWidth, color, lightmap, disableNormals);

        origin.set(maxPos.x(), minPos.y(), maxPos.z());
        bufferCuboidLine(pose, consumer, origin, Direction.UP, lineLengthY, lineWidth, color, lightmap, disableNormals);

        origin.set(maxPos.x(), maxPos.y(), minPos.z());
        bufferCuboidLine(pose, consumer, origin, Direction.SOUTH, lineLengthZ, lineWidth, color, lightmap, disableNormals);
    }

}
