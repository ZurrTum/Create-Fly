package com.zurrtum.create.client.ponder.foundation.render;

import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import org.jspecify.annotations.Nullable;

public interface DynamicTransformsHolder {
    void ponder$updateTransforms(@Nullable GpuBufferSlice dynamicTransforms);
}
