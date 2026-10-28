package com.zurrtum.create.client.flywheel.lib.instance;

import com.zurrtum.create.client.flywheel.api.instance.InstanceHandle;
import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import com.zurrtum.create.client.flywheel.lib.transform.Rotate;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

public abstract class AbstractRotationInstance<T extends AbstractRotationInstance<T>> extends ColoredLitOverlayInstance implements Rotate<T> {
    public final Quaternionf rotation = new Quaternionf();

    public AbstractRotationInstance(InstanceType<? extends ColoredLitOverlayInstance> type, InstanceHandle handle) {
        super(type, handle);
    }

    public T rotation(AbstractRotationInstance<?> instance) {
        rotation.set(instance.rotation);
        return self();
    }

    public T rotation(Quaternionfc q) {
        rotation.set(q);
        return self();
    }

    public T rotation(float x, float y, float z, float w) {
        rotation.set(x, y, z, w);
        return self();
    }

    public T rotation(Vector3fc rotationAxis, float radians) {
        rotation.setAngleAxis(radians, rotationAxis.x(), rotationAxis.y(), rotationAxis.z());
        return self();
    }

    public T rotationX(float radians) {
        rotation.rotationX(radians);
        return self();
    }

    public T rotationY(float radians) {
        rotation.rotationY(radians);
        return self();
    }

    public T rotationZ(float radians) {
        rotation.rotationZ(radians);
        return self();
    }

    public T rotationXYZ(float xRadians, float yRadians, float zRadians) {
        rotation.rotationXYZ(xRadians, yRadians, zRadians);
        return self();
    }

    public T rotationYXZ(float yRadians, float xRadians, float zRadians) {
        rotation.rotationYXZ(yRadians, xRadians, zRadians);
        return self();
    }

    public T rotationZXY(float zRadians, float yRadians, float xRadians) {
        rotation.rotationZYX(zRadians, yRadians, xRadians);
        return self();
    }

    public T identityRotation() {
        rotation.identity();
        return self();
    }

    @Override
    public T rotate(Quaternionfc quaternion) {
        rotation.mul(quaternion);
        return self();
    }

    @Override
    public T rotateX(float radians) {
        rotation.rotateX(radians);
        return self();
    }

    @Override
    public T rotateY(float radians) {
        rotation.rotateY(radians);
        return self();
    }

    @Override
    public T rotateZ(float radians) {
        rotation.rotateZ(radians);
        return self();
    }

    public T rotateXYZ(float xRadians, float yRadians, float zRadians) {
        rotation.rotateXYZ(xRadians, yRadians, zRadians);
        return self();
    }

    public T rotateYXZ(float yRadians, float xRadians, float zRadians) {
        rotation.rotateYXZ(yRadians, xRadians, zRadians);
        return self();
    }

    public T rotateZXY(float zRadians, float yRadians, float xRadians) {
        rotation.rotateZYX(zRadians, yRadians, xRadians);
        return self();
    }

    @Override
    public T rotateTo(float fromX, float fromY, float fromZ, float toX, float toY, float toZ) {
        rotation.rotateTo(fromX, fromY, fromZ, toX, toY, toZ);
        return self();
    }
}
