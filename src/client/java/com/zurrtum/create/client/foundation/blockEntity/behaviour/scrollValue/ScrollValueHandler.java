package com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue;

import com.zurrtum.create.client.catnip.animation.PhysicalFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public class ScrollValueHandler {

    private static float lastPassiveScroll = 0.0f;
    private static float passiveScroll = 0.0f;
    private static float passiveScrollDirection = 1f;
    public static final PhysicalFloat wrenchCog = PhysicalFloat.create().withDrag(0.3);

    public static float getScroll(float partialTicks) {
        return wrenchCog.getValue(partialTicks) + Mth.lerp(partialTicks, lastPassiveScroll, passiveScroll);
    }

    public static void tick(Minecraft client) {
        if (!client.isPaused()) {
            lastPassiveScroll = passiveScroll;
            wrenchCog.tick();
            passiveScroll += passiveScrollDirection * 0.5;
        }
    }

}
