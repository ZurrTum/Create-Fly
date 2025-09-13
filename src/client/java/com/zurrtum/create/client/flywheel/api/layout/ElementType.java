package com.zurrtum.create.client.flywheel.api.layout;

public sealed interface ElementType permits ScalarElementType, VectorElementType, MatrixElementType, ArrayElementType {
    int byteSize();

    int byteAlignment();
}
