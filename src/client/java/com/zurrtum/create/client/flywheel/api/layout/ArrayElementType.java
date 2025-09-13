package com.zurrtum.create.client.flywheel.api.layout;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Range;

@ApiStatus.NonExtendable
public non-sealed interface ArrayElementType extends ElementType {
    ElementType innerType();

    @Range(from = 1L, to = 256L)
    int length();
}
