package com.zurrtum.create.client.flywheel.api.layout;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Range;

@ApiStatus.NonExtendable
public non-sealed interface MatrixElementType extends ElementType {
    FloatRepr repr();

    @Range(from = 2L, to = 4L)
    int rows();

    @Range(from = 2L, to = 4L)
    int columns();
}
