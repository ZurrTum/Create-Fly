package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.content.equipment.armor.CardboardRenderState;
import com.zurrtum.create.client.foundation.render.UuidRenderState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

@Mixin(PlayerEntityRenderState.class)
public class PlayerEntityRenderStateMixin implements CardboardRenderState, UuidRenderState {
    @Unique
    private boolean flying;
    @Unique
    private boolean skip;
    @Unique
    private boolean onGround;
    @Unique
    private float lastYaw;
    @Unique
    private float yaw;
    @Unique
    private double lastX;
    @Unique
    private double lastY;
    @Unique
    private double lastZ;
    @Unique
    private Vec3d pos;
    @Unique
    private float tickProgress;
    @Unique
    private UUID uuid;

    @Override
    public void create$setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public UUID create$getUuid() {
        return uuid;
    }

    @Override
    public void create$update(AbstractClientPlayerEntity player, float tickProgress) {
        if (player.getAbilities().flying) {
            flying = true;
            return;
        } else {
            flying = false;
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        if (player == mc.player && mc.options.getPerspective() == Perspective.FIRST_PERSON) {
            skip = true;
            return;
        } else {
            skip = false;
        }
        onGround = player.isOnGround();
        lastYaw = player.lastYaw;
        yaw = player.getYaw();
        lastX = player.lastX;
        lastY = player.lastY;
        lastZ = player.lastZ;
        pos = player.getPos();
        this.tickProgress = tickProgress;
    }

    @Override
    public boolean create$isFlying() {
        return flying;
    }

    @Override
    public boolean create$isSkip() {
        return skip;
    }

    @Override
    public boolean create$isOnGround() {
        return onGround;
    }

    @Override
    public double create$getMovement() {
        return pos.subtract(lastX, lastY, lastZ).length();
    }

    @Override
    public float create$getInterpolatedYaw() {
        return MathHelper.lerp(tickProgress, lastYaw, yaw);
    }
}
