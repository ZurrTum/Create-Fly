package com.zurrtum.create.client.flywheel.api.layout;

import com.zurrtum.create.client.flywheel.api.internal.FlwApiLink;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Range;

@ApiStatus.NonExtendable
public interface LayoutBuilder {
    LayoutBuilder scalar(String var1, ValueRepr var2);

    LayoutBuilder vector(String var1, ValueRepr var2, @Range(from = 2L, to = 4L) int var3);

    LayoutBuilder matrix(String var1, FloatRepr var2, @Range(from = 2L, to = 4L) int var3, @Range(from = 2L, to = 4L) int var4);

    LayoutBuilder matrix(String var1, FloatRepr var2, @Range(from = 2L, to = 4L) int var3);

    LayoutBuilder scalarArray(String var1, ValueRepr var2, @Range(from = 1L, to = 256L) int var3);

    LayoutBuilder vectorArray(String var1, ValueRepr var2, @Range(from = 2L, to = 4L) int var3, @Range(from = 1L, to = 256L) int var4);

    LayoutBuilder matrixArray(
        String var1,
        FloatRepr var2,
        @Range(from = 2L, to = 4L) int var3,
        @Range(from = 2L, to = 4L) int var4,
        @Range(from = 1L, to = 256L) int var5
    );

    LayoutBuilder matrixArray(String var1, FloatRepr var2, @Range(from = 2L, to = 4L) int var3, @Range(from = 1L, to = 256L) int var4);

    Layout build();

    static LayoutBuilder create() {
        return FlwApiLink.INSTANCE.createLayoutBuilder();
    }
}
