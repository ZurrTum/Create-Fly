package com.zurrtum.create.client.content.equipment.armor;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.content.equipment.armor.CardboardArmorHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;

public class CardboardArmorStealthOverlay {
    private static final LerpedFloat opacity = LerpedFloat.linear().startWithValue(0).chase(0, 0.25f, Chaser.EXP);

    public static void clientTick(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        if (player == null)
            return;

        opacity.tickChaser();
        opacity.updateChaseTarget(CardboardArmorHandler.testForStealth(player) ? 1 : 0);
    }

    public static float getOverlayOpacity(RenderTickCounter tickCounter) {
        return opacity.getValue(tickCounter.getTickProgress(true));
    }
}
