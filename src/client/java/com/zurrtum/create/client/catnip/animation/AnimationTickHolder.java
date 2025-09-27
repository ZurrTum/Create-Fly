package com.zurrtum.create.client.catnip.animation;

import com.zurrtum.create.client.catnip.levelWrappers.WrappedClientLevel;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.foundation.ui.PonderUI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.world.WorldAccess;

public class AnimationTickHolder {

    private static int ticks;
    private static int pausedTicks;

    public static void reset() {
        ticks = 0;
        pausedTicks = 0;
    }

    public static void tick() {
        if (!MinecraftClient.getInstance().isPaused()) {
            ticks = (ticks + 1) % 1_728_000; // wrap around every 24 hours so we maintain enough floating point precision
        } else {
            pausedTicks = (pausedTicks + 1) % 1_728_000;
        }
    }

    public static int getTicks() {
        return getTicks(false);
    }

    public static int getTicks(boolean includePaused) {
        return includePaused ? ticks + pausedTicks : ticks;
    }

    public static int getTicks(WorldAccess level) {
        if (level instanceof WrappedClientLevel wrappedLevel) {
            return getTicks(wrappedLevel.getWrappedLevel());
        } else if (level instanceof PonderLevel) {
            return PonderUI.ponderTicks;
        }
        return getTicks();
    }

    public static float getPartialTicks(WorldAccess level) {
        if (level instanceof PonderLevel) {
            return PonderUI.getPartialTicks();
        }
        return getPartialTicks();
    }

    public static float getRenderTime() {
        return getTicks() + getPartialTicks();
    }

    public static float getRenderTime(WorldAccess level) {
        return getTicks(level) + getPartialTicks(level);
    }

    /**
     * @return the fraction between the current tick to the next tick, frozen during game pause [0-1]
     */
    public static float getPartialTicks() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc.getRenderTickCounter().getTickProgress(false);
    }

    /**
     * @return the fraction between the current tick to the next tick, not frozen during game pause [0-1]
     */
    public static float getPartialTicksUI(RenderTickCounter timer) {
        if (timer instanceof RenderTickCounter.Dynamic timerAccessor) {
            return timerAccessor.tickProgress;
        } else {
            return timer.getTickProgress(false);
        }
    }
}