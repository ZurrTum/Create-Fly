package com.zurrtum.create.client.content.equipment.armor;

import net.minecraft.client.network.ClientPlayerLikeEntity;
import net.minecraft.entity.PlayerLikeEntity;

public interface CardboardRenderState {
    double create$getMovement();

    float create$getInterpolatedYaw();

    boolean create$isFlying();

    boolean create$isSkip();

    boolean create$isOnGround();

    <T extends PlayerLikeEntity & ClientPlayerLikeEntity> void create$update(T player, float tickProgress);
}
