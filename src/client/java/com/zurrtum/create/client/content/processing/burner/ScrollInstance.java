package com.zurrtum.create.client.content.processing.burner;

import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.flywheel.api.instance.InstanceHandle;
import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import com.zurrtum.create.client.flywheel.lib.instance.ColoredLitOverlayInstance;
import net.minecraft.util.math.Vec3i;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public class ScrollInstance extends ColoredLitOverlayInstance {
    public float x;
    public float y;
    public float z;
    public final Quaternionf rotation = new Quaternionf();

    public float speedU;
    public float speedV;

    public float offsetU;
    public float offsetV;

    public float diffU;
    public float diffV;

    public float scaleU;
    public float scaleV;

    public ScrollInstance(InstanceType<? extends ColoredLitOverlayInstance> type, InstanceHandle handle) {
        super(type, handle);
    }

    public ScrollInstance position(Vec3i position) {
        this.x = position.getX();
        this.y = position.getY();
        this.z = position.getZ();
        return this;
    }

    public ScrollInstance position(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public ScrollInstance shift(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public ScrollInstance rotation(Quaternionfc rotation) {
        this.rotation.set(rotation);
        return this;
    }

    public ScrollInstance setSpriteShift(SpriteShiftEntry spriteShift) {
        return setSpriteShift(spriteShift, 0.5f, 0.5f);
    }

    public ScrollInstance setSpriteShift(SpriteShiftEntry spriteShift, float factorU, float factorV) {
        float spriteWidth = spriteShift.getTarget().getMaxU() - spriteShift.getTarget().getMinU();

        float spriteHeight = spriteShift.getTarget().getMaxV() - spriteShift.getTarget().getMinV();

        scaleU = spriteWidth * factorU;
        scaleV = spriteHeight * factorV;

        diffU = spriteShift.getTarget().getMinU() - spriteShift.getOriginal().getMinU();
        diffV = spriteShift.getTarget().getMinV() - spriteShift.getOriginal().getMinV();

        return this;
    }

    public ScrollInstance speed(float speedU, float speedV) {
        this.speedU = speedU;
        this.speedV = speedV;
        return this;
    }

    public ScrollInstance offset(float offsetU, float offsetV) {
        this.offsetU = offsetU;
        this.offsetV = offsetV;
        return this;
    }
}
