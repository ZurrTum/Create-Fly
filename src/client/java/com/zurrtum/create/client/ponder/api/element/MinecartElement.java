package com.zurrtum.create.client.ponder.api.element;

import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface MinecartElement extends AnimatedSceneElement {
    void setPositionOffset(Vec3d position, boolean immediate);

    void setRotation(float angle, boolean immediate);

    Vec3d getPositionOffset();

    Vec3d getRotation();

    interface MinecartConstructor {
        default AbstractMinecartEntity create(World w, double x, double y, double z) {
            AbstractMinecartEntity minecart = create(w, SpawnReason.LOAD);
            minecart.initPosition(x, y, z);
            return minecart;
        }

        AbstractMinecartEntity create(World w, SpawnReason reason);
    }
}