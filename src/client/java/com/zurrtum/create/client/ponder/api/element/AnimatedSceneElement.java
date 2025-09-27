package com.zurrtum.create.client.ponder.api.element;

import net.minecraft.util.math.Vec3d;

public interface AnimatedSceneElement extends PonderSceneElement {
    void forceApplyFade(float fade);

    void setFade(float fade);

    void setFadeVec(Vec3d fadeVec);
}
