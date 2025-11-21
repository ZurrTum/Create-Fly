package com.zurrtum.create.client.ponder.api.element;

import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface MinecartElement extends AnimatedSceneElement {
    void setPositionOffset(Vec3 position, boolean immediate);

    void setRotation(float angle, boolean immediate);

    Vec3 getPositionOffset();

    Vec3 getRotation();

    interface MinecartConstructor {
        default AbstractMinecart create(Level w, double x, double y, double z) {
            AbstractMinecart minecart = create(w, EntitySpawnReason.LOAD);
            minecart.setInitialPos(x, y, z);
            return minecart;
        }

        AbstractMinecart create(Level w, EntitySpawnReason reason);
    }
}