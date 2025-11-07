package com.zurrtum.create.client.flywheel.api.layout;

import com.zurrtum.create.client.flywheel.api.internal.FlwApiLink;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Range;

@ApiStatus.NonExtendable
public interface LayoutBuilder {
    LayoutBuilder scalar(String name, ValueRepr repr);

    LayoutBuilder vector(String name, ValueRepr repr, @Range(from = 2L, to = 4L) int size);

    LayoutBuilder matrix(String name, FloatRepr repr, @Range(from = 2L, to = 4L) int rows, @Range(from = 2L, to = 4L) int columns);

    LayoutBuilder matrix(String name, FloatRepr repr, @Range(from = 2L, to = 4L) int size);

    LayoutBuilder scalarArray(String name, ValueRepr repr, @Range(from = 1L, to = 256L) int length);

    LayoutBuilder vectorArray(String name, ValueRepr repr, @Range(from = 2L, to = 4L) int size, @Range(from = 1L, to = 256L) int length);

    LayoutBuilder matrixArray(
        String name,
        FloatRepr repr,
        @Range(from = 2L, to = 4L) int rows,
        @Range(from = 2L, to = 4L) int columns,
        @Range(from = 1L, to = 256L) int length
    );

    LayoutBuilder matrixArray(String name, FloatRepr repr, @Range(from = 2L, to = 4L) int size, @Range(from = 1L, to = 256L) int length);

    Layout build();

    static LayoutBuilder create() {
        return FlwApiLink.INSTANCE.createLayoutBuilder();
    }
}
