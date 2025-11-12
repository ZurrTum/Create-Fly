package com.zurrtum.create.client.flywheel.lib.transform;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;

public interface Transform<Self extends Transform<Self>> extends Affine<Self> {
    Self mulPose(Matrix4fc pose);

    Self mulNormal(Matrix3fc normal);

    default Self transform(Matrix4fc pose, Matrix3fc normal) {
        return mulPose(pose).mulNormal(normal);
    }

    default Self transform(PoseStack.Pose pose) {
        return transform(pose.pose(), pose.normal());
    }

    default Self transform(PoseStack stack) {
        return transform(stack.last());
    }
}
