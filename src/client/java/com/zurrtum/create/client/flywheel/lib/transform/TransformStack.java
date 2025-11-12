package com.zurrtum.create.client.flywheel.lib.transform;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.flywheel.lib.internal.FlwLibLink;

public interface TransformStack<Self extends TransformStack<Self>> extends Transform<Self> {
    static PoseTransformStack of(PoseStack stack) {
        return FlwLibLink.INSTANCE.getPoseTransformStackOf(stack);
    }

    Self pushPose();

    Self popPose();
}
