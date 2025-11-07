package com.zurrtum.create.client.flywheel.api.vertex;

/**
 * A read only view of a vertex buffer.
 *
 * <p>
 * VertexList assumes nothing about the layout of the vertices. Implementations should feel free to return constants
 * for values that are unused in their layout.
 * </p>
 */
public interface VertexList {
    float x(int index);

    float y(int index);

    float z(int index);

    float r(int index);

    float g(int index);

    float b(int index);

    float a(int index);

    float u(int index);

    float v(int index);

    int overlay(int index);

    int light(int index);

    float normalX(int index);

    float normalY(int index);

    float normalZ(int index);

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
