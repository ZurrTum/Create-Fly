package com.zurrtum.create.client.content.equipment.armor;

import net.minecraft.client.network.AbstractClientPlayerEntity;

public interface CardboardRenderState {
    double create$getMovement();

    float create$getInterpolatedYaw();

    boolean create$isFlying();

    boolean create$isSkip();

    boolean create$isOnGround();

    void create$update(AbstractClientPlayerEntity player, float tickProgress);
}
