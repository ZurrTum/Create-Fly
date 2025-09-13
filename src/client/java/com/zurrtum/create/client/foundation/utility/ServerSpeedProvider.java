package com.zurrtum.create.client.foundation.utility;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import net.minecraft.client.MinecraftClient;

public class ServerSpeedProvider {
    public static final LerpedFloat modifier = LerpedFloat.linear();
    public static int clientTimer = 0;
    public static boolean initialized = false;

    public static void clientTick(MinecraftClient mc) {
        if (mc.isIntegratedServerRunning() && mc.isPaused())
            return;
        modifier.tickChaser();
        clientTimer++;
    }

    public static float get() {
        return modifier.getValue();
    }
}
