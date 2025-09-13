package com.zurrtum.create.client.content.kinetics.base;

import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.content.kinetics.KineticDebugger;
import com.zurrtum.create.client.flywheel.api.instance.InstanceHandle;
import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import com.zurrtum.create.client.flywheel.lib.instance.ColoredLitOverlayInstance;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3i;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class RotatingInstance extends ColoredLitOverlayInstance {
    public static final float SPEED_MULTIPLIER = 6;

    public byte rotationAxisX;
    public byte rotationAxisY;
    public byte rotationAxisZ;
    public float x;
    public float y;
    public float z;
    /**
     * Speed in degrees per second
     */
    public float rotationalSpeed;
    /**
     * Offset in degrees
     */
    public float rotationOffset;

    /**
     * Base rotation of the instance, applied before kinetic rotation
     */
    public final Quaternionf rotation = new Quaternionf();

    public RotatingInstance(InstanceType<? extends RotatingInstance> type, InstanceHandle handle) {
        super(type, handle);
    }

    public static int colorFromBE(KineticBlockEntity be) {
        if (be.hasNetwork())
            return Color.generateFromLong(be.network).getRGB();
        return 0xFFFFFF;
    }

    public RotatingInstance setup(KineticBlockEntity blockEntity) {
        var blockState = blockEntity.getCachedState();
        var axis = KineticBlockEntityVisual.rotationAxis(blockState);
        return setup(blockEntity, axis, blockEntity.getSpeed());
    }

    public RotatingInstance setup(KineticBlockEntity blockEntity, Axis axis) {
        return setup(blockEntity, axis, blockEntity.getSpeed());
    }

    public RotatingInstance setup(KineticBlockEntity blockEntity, float speed) {
        var blockState = blockEntity.getCachedState();
        var axis = KineticBlockEntityVisual.rotationAxis(blockState);
        return setup(blockEntity, axis, speed);
    }

    public RotatingInstance setup(KineticBlockEntity blockEntity, Axis axis, float speed) {
        var blockState = blockEntity.getCachedState();
        var pos = blockEntity.getPos();
        var instance = setRotationAxis(axis).setRotationalSpeed(speed * RotatingInstance.SPEED_MULTIPLIER)
            .setRotationOffset(KineticBlockEntityVisual.rotationOffset(blockState, axis, pos) + blockEntity.getRotationAngleOffset(axis));

        if (KineticDebugger.isActive())
            instance.setColor(blockEntity);

        return instance;
    }

    public RotatingInstance rotateToFace(Direction.Axis axis) {
        Direction orientation = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        return rotateToFace(orientation);
    }

    public RotatingInstance rotateToFace(Direction from, Direction.Axis axis) {
        Direction orientation = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        return rotateToFace(from, orientation);
    }

    public RotatingInstance rotateToFace(Direction orientation) {
        return rotateToFace(orientation.getOffsetX(), orientation.getOffsetY(), orientation.getOffsetZ());
    }

    public RotatingInstance rotateToFace(Direction from, Direction orientation) {
        return rotateTo(
            from.getOffsetX(),
            from.getOffsetY(),
            from.getOffsetZ(),
            orientation.getOffsetX(),
            orientation.getOffsetY(),
            orientation.getOffsetZ()
        );
    }

    public RotatingInstance rotateToFace(float stepX, float stepY, float stepZ) {
        return rotateTo(0, 1, 0, stepX, stepY, stepZ);
    }

    public RotatingInstance rotateTo(float fromX, float fromY, float fromZ, float toX, float toY, float toZ) {
        rotation.rotateTo(fromX, fromY, fromZ, toX, toY, toZ);
        return this;
    }

    public RotatingInstance setRotationAxis(Direction.Axis axis) {
        Direction orientation = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        return setRotationAxis(orientation.getUnitVector());
    }

    public RotatingInstance setRotationAxis(Vector3f axis) {
        return setRotationAxis(axis.x(), axis.y(), axis.z());
    }

    public RotatingInstance setRotationAxis(float rotationAxisX, float rotationAxisY, float rotationAxisZ) {
        this.rotationAxisX = (byte) (rotationAxisX * 127);
        this.rotationAxisY = (byte) (rotationAxisY * 127);
        this.rotationAxisZ = (byte) (rotationAxisZ * 127);
        return this;
    }

    public RotatingInstance setPosition(Vec3i pos) {
        return setPosition(pos.getX(), pos.getY(), pos.getZ());
    }

    public RotatingInstance setPosition(Vector3f pos) {
        return setPosition(pos.x(), pos.y(), pos.z());
    }

    public RotatingInstance setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public RotatingInstance nudge(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public RotatingInstance setColor(KineticBlockEntity blockEntity) {
        colorRgb(colorFromBE(blockEntity));
        return this;
    }

    public RotatingInstance setColor(Color c) {
        color(c.getRed(), c.getGreen(), c.getBlue());
        return this;
    }

    public RotatingInstance setRotationalSpeed(float rotationalSpeed) {
        this.rotationalSpeed = rotationalSpeed;
        return this;
    }

    public RotatingInstance setRotationOffset(float rotationOffset) {
        this.rotationOffset = rotationOffset;
        return this;
    }
}
