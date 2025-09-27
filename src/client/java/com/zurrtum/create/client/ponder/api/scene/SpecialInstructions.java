package com.zurrtum.create.client.ponder.api.scene;

import com.zurrtum.create.client.ponder.api.element.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.function.Supplier;

public interface SpecialInstructions {
    ElementLink<ParrotElement> createBirb(Vec3d location, Supplier<? extends ParrotPose> pose);

    void changeBirbPose(ElementLink<ParrotElement> birb, Supplier<? extends ParrotPose> pose);

    void movePointOfInterest(Vec3d location);

    void movePointOfInterest(BlockPos location);

    void rotateParrot(ElementLink<ParrotElement> link, double xRotation, double yRotation, double zRotation, int duration);

    void moveParrot(ElementLink<ParrotElement> link, Vec3d offset, int duration);

    ElementLink<MinecartElement> createCart(Vec3d location, float angle, MinecartElement.MinecartConstructor type);

    void rotateCart(ElementLink<MinecartElement> link, float yRotation, int duration);

    void moveCart(ElementLink<MinecartElement> link, Vec3d offset, int duration);

    <T extends AnimatedSceneElement> void hideElement(ElementLink<T> link, Direction direction);
}