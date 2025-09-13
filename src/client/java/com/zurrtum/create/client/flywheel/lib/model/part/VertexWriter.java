package com.zurrtum.create.client.flywheel.lib.model.part;

import com.zurrtum.create.client.flywheel.lib.math.DataPacker;
import com.zurrtum.create.client.flywheel.lib.memory.MemoryBlock;
import com.zurrtum.create.client.flywheel.lib.vertex.PosTexNormalVertexView;
import net.minecraft.client.render.VertexConsumer;
import org.lwjgl.system.MemoryUtil;

public class VertexWriter implements VertexConsumer {
    private static final int STRIDE = (int) PosTexNormalVertexView.STRIDE;

    private MemoryBlock data;

    private int vertexCount;
    private boolean filledTexture = true;
    private boolean filledNormal = true;

    public VertexWriter() {
        data = MemoryBlock.malloc(128 * STRIDE);
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        endLastVertex();
        vertexCount++;

        long byteSize = vertexCount * STRIDE;
        long capacity = data.size();
        if (byteSize > capacity) {
            data = data.realloc(capacity * 2);
        }

        filledTexture = false;
        filledNormal = false;

        long ptr = vertexPtr();
        MemoryUtil.memPutFloat(ptr, x);
        MemoryUtil.memPutFloat(ptr + 4, y);
        MemoryUtil.memPutFloat(ptr + 8, z);
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        // ignore color
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        if (!filledTexture) {
            long ptr = vertexPtr();
            MemoryUtil.memPutFloat(ptr + 12, u);
            MemoryUtil.memPutFloat(ptr + 16, v);
            filledTexture = true;
        }
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        // ignore overlay
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        // ignore light
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        if (!filledNormal) {
            long ptr = vertexPtr();
            MemoryUtil.memPutByte(ptr + 20, DataPacker.packNormI8(x));
            MemoryUtil.memPutByte(ptr + 21, DataPacker.packNormI8(y));
            MemoryUtil.memPutByte(ptr + 22, DataPacker.packNormI8(z));
            filledNormal = true;
        }
        return this;
    }

    private long vertexPtr() {
        return data.ptr() + (vertexCount - 1) * STRIDE;
    }

    private void endLastVertex() {
        if (vertexCount != 0) {
            if (!filledTexture || !filledNormal) {
                throw new IllegalStateException("Missing elements in vertex");
            }
        }
    }

    public MemoryBlock copyDataAndReset() {
        endLastVertex();

        MemoryBlock dataCopy = MemoryBlock.mallocTracked(vertexCount * STRIDE);
        data.copyTo(dataCopy);

        vertexCount = 0;
        filledTexture = true;
        filledNormal = true;

        return dataCopy;
    }
}