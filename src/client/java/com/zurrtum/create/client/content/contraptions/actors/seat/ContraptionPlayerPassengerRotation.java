package com.zurrtum.create.client.content.contraptions.actors.seat;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity.ContraptionRotationState;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.PositionAndRotation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class ContraptionPlayerPassengerRotation {
    static int prevId;
    static float prevYaw;
    static float prevPitch;

    public static void tick(Minecraft mc) {
        if (!AllConfigs.client().rotateWhenSeated.get()) {
            return;
        }
        Player player = mc.player;
        if (player == null || !player.isPassenger()) {
            prevId = 0;
            return;
        }

        Entity vehicle = player.getVehicle();
        if (!(vehicle instanceof AbstractContraptionEntity contraptionEntity)) {
            return;
        }

        ContraptionRotationState rotationState = contraptionEntity.getRotationState();

        float yaw = AngleHelper.wrapAngle180(
            contraptionEntity instanceof CarriageContraptionEntity cce ? cce.getViewYRot(1) : rotationState.yRotation);

        float pitch = contraptionEntity instanceof CarriageContraptionEntity cce ? cce.getViewXRot(1) : 0;

        if (prevId != contraptionEntity.getId()) {
            prevId = contraptionEntity.getId();
            prevYaw = yaw;
            prevPitch = pitch;
        }

        float yawDiff = AngleHelper.getShortestAngleDiff(yaw, prevYaw);
        float pitchDiff = AngleHelper.getShortestAngleDiff(pitch, prevPitch);

        prevYaw = yaw;
        prevPitch = pitch;

        PositionAndRotation positionAndRotation = player.getClientPositionAndRotation();
        float playerYaw = positionAndRotation.yRot();
        float yawRelativeToTrain = Mth.abs(AngleHelper.getShortestAngleDiff(playerYaw, -yaw - 90));
        if (yawRelativeToTrain > 120) {
            pitchDiff *= -1;
        } else if (yawRelativeToTrain > 60) {
            pitchDiff *= 0;
        }

        player.moveOrInterpolateTo(playerYaw + yawDiff, positionAndRotation.xRot() + pitchDiff);
    }
}
