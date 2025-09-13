package com.zurrtum.create.content.contraptions.bearing;

import com.zurrtum.create.content.contraptions.IControlContraption;

public interface IBearingBlockEntity extends IControlContraption {

    float getInterpolatedAngle(float partialTicks);

    boolean isWoodenTop();

    void setAngle(float forcedAngle);

}
