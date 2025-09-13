package com.zurrtum.create.client.content.processing.burner;

import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.flywheel.api.instance.InstanceHandle;
import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;

public class ScrollTransformedInstance extends TransformedInstance {
    public float speedU;
    public float speedV;

    public float offsetU;
    public float offsetV;

    public float diffU;
    public float diffV;

    public float scaleU;
    public float scaleV;

    public ScrollTransformedInstance(InstanceType<? extends TransformedInstance> type, InstanceHandle handle) {
        super(type, handle);
    }

    public ScrollTransformedInstance setSpriteShift(SpriteShiftEntry spriteShift) {
        return setSpriteShift(spriteShift, 0.5f, 0.5f);
    }

    public ScrollTransformedInstance setSpriteShift(SpriteShiftEntry spriteShift, float factorU, float factorV) {
        float spriteWidth = spriteShift.getTarget().getMaxU() - spriteShift.getTarget().getMinU();

        float spriteHeight = spriteShift.getTarget().getMaxV() - spriteShift.getTarget().getMinV();

        scaleU = spriteWidth * factorU;
        scaleV = spriteHeight * factorV;

        diffU = spriteShift.getTarget().getMinU() - spriteShift.getOriginal().getMinU();
        diffV = spriteShift.getTarget().getMinV() - spriteShift.getOriginal().getMinV();

        return this;
    }

    public ScrollTransformedInstance speed(float speedU, float speedV) {
        this.speedU = speedU;
        this.speedV = speedV;
        return this;
    }

    public ScrollTransformedInstance offset(float offsetU, float offsetV) {
        this.offsetU = offsetU;
        this.offsetV = offsetV;
        return this;
    }
}
