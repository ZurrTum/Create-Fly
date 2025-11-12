package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.content.equipment.armor.CardboardRenderState;
import com.zurrtum.create.client.foundation.render.SkyhookRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

@Mixin(AvatarRenderState.class)
public class PlayerEntityRenderStateMixin implements CardboardRenderState, SkyhookRenderState {
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
    private Vec3 pos;
    @Unique
    private float tickProgress;
    @Unique
    private UUID uuid;
    @Unique
    private ItemStack mainStack;

    @Override
    public void create$setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public UUID create$getUuid() {
        return uuid;
    }

    @Override
    public void create$setMainStack(ItemStack stack) {
        mainStack = stack;
    }

    @Override
    public ItemStack create$getMainStack() {
        return mainStack;
    }

    @Override
    public <T extends Avatar & ClientAvatarEntity> void create$update(T player, float tickProgress) {
        if (player instanceof AbstractClientPlayer clientPlayer && clientPlayer.getAbilities().flying) {
            flying = true;
            return;
        } else {
            flying = false;
        }
        Minecraft mc = Minecraft.getInstance();
        if (player == mc.player && mc.options.getCameraType() == CameraType.FIRST_PERSON) {
            skip = true;
            return;
        } else {
            skip = false;
        }
        onGround = player.onGround();
        lastYaw = player.yRotO;
        yaw = player.getYRot();
        lastX = player.xo;
        lastY = player.yo;
        lastZ = player.zo;
        pos = player.position();
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
        return Mth.lerp(tickProgress, lastYaw, yaw);
    }
}
