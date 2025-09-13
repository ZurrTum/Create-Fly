package com.zurrtum.create.client.flywheel.lib.transform;

import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;

public interface Transform<Self extends Transform<Self>> extends Affine<Self> {
    Self mulPose(Matrix4fc pose);

    Self mulNormal(Matrix3fc normal);

    default Self transform(Matrix4fc pose, Matrix3fc normal) {
        return mulPose(pose).mulNormal(normal);
    }

    default Self transform(MatrixStack.Entry pose) {
        return transform(pose.getPositionMatrix(), pose.getNormalMatrix());
    }

    default Self transform(MatrixStack stack) {
        return transform(stack.peek());
    }
}
