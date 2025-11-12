package com.zurrtum.create.client.content.equipment.armor;

import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.world.entity.Avatar;

public interface CardboardRenderState {
    double create$getMovement();

    float create$getInterpolatedYaw();

    boolean create$isFlying();

    boolean create$isSkip();

    boolean create$isOnGround();

    <T extends Avatar & ClientAvatarEntity> void create$update(T player, float tickProgress);
}
