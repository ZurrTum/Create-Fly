package com.zurrtum.create.client.ponder.api.element;

import net.minecraft.util.math.Vec3d;

public interface ParrotElement extends AnimatedSceneElement {
    void setPositionOffset(Vec3d position, boolean immediate);

    void setRotation(Vec3d eulers, boolean immediate);

    Vec3d getPositionOffset();

    Vec3d getRotation();

    void setPose(ParrotPose pose);
}