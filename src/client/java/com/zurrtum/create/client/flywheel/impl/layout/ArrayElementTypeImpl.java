package com.zurrtum.create.client.flywheel.impl.layout;

import com.zurrtum.create.client.flywheel.api.layout.ArrayElementType;
import com.zurrtum.create.client.flywheel.api.layout.ElementType;
import org.jetbrains.annotations.Range;

record ArrayElementTypeImpl(
    ElementType innerType, @Range(from = 1, to = 256) int length, int byteSize, int byteAlignment
) implements ArrayElementType {
    static ArrayElementTypeImpl create(ElementType innerType, @Range(from = 1, to = 256) int length) {
        if (length < 1 || length > 256) {
            throw new IllegalArgumentException("Array element length must be in range [1, 256]!");
        }

        return new ArrayElementTypeImpl(innerType, length, innerType.byteSize() * length, innerType.byteAlignment());
    }
}
