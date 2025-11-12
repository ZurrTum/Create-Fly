package com.zurrtum.create.client.content.contraptions.actors.seat;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity.ContraptionRotationState;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class ContraptionPlayerPassengerRotation {

    static boolean active;
    static int prevId;
    static float prevYaw;
    static float prevPitch;

    public static void tick() {
        active = AllConfigs.client().rotateWhenSeated.get();
    }

    public static void frame(Minecraft client) {
        Player player = client.player;
        if (!active)
            return;
        if (player == null || !player.isPassenger()) {
            prevId = 0;
            return;
        }

        Entity vehicle = player.getVehicle();
        if (!(vehicle instanceof AbstractContraptionEntity contraptionEntity))
            return;

        ContraptionRotationState rotationState = contraptionEntity.getRotationState();

        float yaw = AngleHelper.wrapAngle180((contraptionEntity instanceof CarriageContraptionEntity cce) ? cce.getViewYRot(AnimationTickHolder.getPartialTicks()) : rotationState.yRotation);

        float pitch = (contraptionEntity instanceof CarriageContraptionEntity cce) ? cce.getViewXRot(AnimationTickHolder.getPartialTicks()) : 0;

        if (prevId != contraptionEntity.getId()) {
            prevId = contraptionEntity.getId();
            prevYaw = yaw;
            prevPitch = pitch;
        }

        float yawDiff = AngleHelper.getShortestAngleDiff(yaw, prevYaw);
        float pitchDiff = AngleHelper.getShortestAngleDiff(pitch, prevPitch);

        prevYaw = yaw;
        prevPitch = pitch;

        float playerYaw = player.getYRot();
        float yawRelativeToTrain = Mth.abs(AngleHelper.getShortestAngleDiff(playerYaw, -yaw - 90));
        if (yawRelativeToTrain > 120)
            pitchDiff *= -1;
        else if (yawRelativeToTrain > 60)
            pitchDiff *= 0;

        player.setYRot(playerYaw + yawDiff);
        player.setXRot(player.getXRot() + pitchDiff);
    }

}
