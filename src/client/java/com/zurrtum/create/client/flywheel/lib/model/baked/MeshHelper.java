package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.MeshData;
import com.zurrtum.create.client.flywheel.lib.memory.MemoryBlock;
import com.zurrtum.create.client.flywheel.lib.model.SimpleQuadMesh;
import com.zurrtum.create.client.flywheel.lib.vertex.NoOverlayVertexView;
import com.zurrtum.create.client.flywheel.lib.vertex.VertexView;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public final class MeshHelper {
    private MeshHelper() {
    }

    public static SimpleQuadMesh blockVerticesToMesh(MeshData data, @Nullable String meshDescriptor) {
        MeshData.DrawState drawState = data.drawState();
        int vertexCount = drawState.vertexCount();
        long srcStride = drawState.format().getVertexSize();

        VertexView vertexView = new NoOverlayVertexView();
        long dstStride = vertexView.stride();

        ByteBuffer src = data.vertexBuffer();
        MemoryBlock dst = MemoryBlock.mallocTracked((long) vertexCount * dstStride);
        long srcPtr = MemoryUtil.memAddress(src);
        long dstPtr = dst.ptr();
        // The first 31 bytes of each vertex in a block vertex buffer are guaranteed to contain the same data in the
        // same order regardless of whether the format is extended by mods like Iris or OptiFine. Copy these bytes and
        // ignore the rest.
        long bytesToCopy = Math.min(dstStride, 31);

        for (int i = 0; i < vertexCount; i++) {
            // It is safe to copy bytes directly since the NoOverlayVertexView uses the same memory layout as the first
            // 31 bytes of the block vertex format, vanilla or otherwise.
            MemoryUtil.memCopy(srcPtr + srcStride * i, dstPtr + dstStride * i, bytesToCopy);
        }

        vertexView.ptr(dstPtr);
        vertexView.vertexCount(vertexCount);
        vertexView.nativeMemoryOwner(dst);

        return new SimpleQuadMesh(vertexView, meshDescriptor);
    }
}
