package com.zurrtum.create.client.flywheel.impl.extension;

import com.zurrtum.create.client.flywheel.lib.transform.PoseTransformStack;
import net.minecraft.client.util.math.MatrixStack;

/**
 * An extension interface for {@link MatrixStack} that provides a {@link PoseTransformStack} wrapper.
 * <br>
 * Each PoseStack lazily creates and saves a wrapper instance. This wrapper is cached and reused for all future calls.
 */
public interface PoseStackExtension {
    /**
     * @return The {@link PoseTransformStack} wrapper for this {@link MatrixStack}.
     */
    PoseTransformStack flywheel$transformStack();
}
