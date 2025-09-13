package com.zurrtum.create.client.flywheel.lib.transform;

import com.zurrtum.create.client.flywheel.lib.internal.FlwLibLink;
import net.minecraft.client.util.math.MatrixStack;

public interface TransformStack<Self extends TransformStack<Self>> extends Transform<Self> {
    static PoseTransformStack of(MatrixStack stack) {
        return FlwLibLink.INSTANCE.getPoseTransformStackOf(stack);
    }

    Self pushPose();

    Self popPose();
}
