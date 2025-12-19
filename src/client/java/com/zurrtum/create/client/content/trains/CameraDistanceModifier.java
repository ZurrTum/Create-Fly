package com.zurrtum.create.client.content.trains;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public class CameraDistanceModifier {
    private static final LerpedFloat multiplier = LerpedFloat.linear().startWithValue(1);

    public static float getMultiplier() {
        return getMultiplier(AnimationTickHolder.getPartialTicks());
    }

    public static float getMultiplier(float partialTicks) {
        return multiplier.getValue(partialTicks);
    }

    public static void onMount(Entity entity, Entity vehicle, boolean mount) {
        if (entity == Minecraft.getInstance().player && vehicle instanceof CarriageContraptionEntity) {
            if (mount) {
                zoomOut();
            } else {
                reset();
            }
        }
    }

    public static void tick() {
        multiplier.tickChaser();
    }

    public static void reset() {
        multiplier.chase(1, 0.1, LerpedFloat.Chaser.EXP);
    }

    public static void zoomOut() {
        zoomOut(AllConfigs.client().mountedZoomMultiplier.getF());
    }

    public static void zoomOut(float targetMultiplier) {
        multiplier.chase(targetMultiplier, 0.075, LerpedFloat.Chaser.EXP);
    }
}
