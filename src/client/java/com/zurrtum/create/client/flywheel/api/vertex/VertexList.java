package com.zurrtum.create.client.flywheel.api.vertex;

public interface VertexList {
    float x(int var1);

    float y(int var1);

    float z(int var1);

    float r(int var1);

    float g(int var1);

    float b(int var1);

    float a(int var1);

    float u(int var1);

    float v(int var1);

    int overlay(int var1);

    int light(int var1);

    float normalX(int var1);

    float normalY(int var1);

    float normalZ(int var1);

    default void write(MutableVertexList dst, int srcIndex, int dstIndex) {
        dst.x(dstIndex, this.x(srcIndex));
        dst.y(dstIndex, this.y(srcIndex));
        dst.z(dstIndex, this.z(srcIndex));
        dst.r(dstIndex, this.r(srcIndex));
        dst.g(dstIndex, this.g(srcIndex));
        dst.b(dstIndex, this.b(srcIndex));
        dst.a(dstIndex, this.a(srcIndex));
        dst.u(dstIndex, this.u(srcIndex));
        dst.v(dstIndex, this.v(srcIndex));
        dst.overlay(dstIndex, this.overlay(srcIndex));
        dst.light(dstIndex, this.light(srcIndex));
        dst.normalX(dstIndex, this.normalX(srcIndex));
        dst.normalY(dstIndex, this.normalY(srcIndex));
        dst.normalZ(dstIndex, this.normalZ(srcIndex));
    }

    default void write(MutableVertexList dst, int srcStartIndex, int dstStartIndex, int vertexCount) {
        for (int i = 0; i < vertexCount; ++i) {
            this.write(dst, srcStartIndex + i, dstStartIndex + i);
        }

    }

    default void writeAll(MutableVertexList dst) {
        this.write(dst, 0, 0, Math.min(this.vertexCount(), dst.vertexCount()));
    }

    int vertexCount();

    default boolean isEmpty() {
        return this.vertexCount() == 0;
    }
}
