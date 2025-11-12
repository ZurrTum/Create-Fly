package com.zurrtum.create.client.content.trains.track;

import com.mojang.blaze3d.platform.Window;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.trains.track.TrackPlacement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

public class TrackPlacementOverlay {
    public static void render(Minecraft mc, GuiGraphics guiGraphics) {
        if (TrackPlacement.hoveringPos == null)
            return;
        if (TrackPlacement.cached == null || TrackPlacement.cached.curve == null || !TrackPlacement.cached.valid)
            return;
        if (TrackPlacementClient.extraTipWarmup < 4)
            return;

        if (mc.gui.toolHighlightTimer > 0)
            return;

        boolean active = mc.options.keySprint.isDown();
        MutableComponent text = CreateLang.translateDirect(
            "track.hold_for_smooth_curve",
            Component.keybind("key.sprint").withStyle(active ? ChatFormatting.WHITE : ChatFormatting.GRAY)
        );

        Window window = mc.getWindow();
        int x = (window.getGuiScaledWidth() - mc.font.width(text)) / 2;
        int y = window.getGuiScaledHeight() - 61;
        Color color = new Color(0x4ADB4A).setAlpha(Mth.clamp((TrackPlacementClient.extraTipWarmup - 4) / 3f, 0.1f, 1));
        guiGraphics.drawString(mc.font, text, x, y, color.getRGB(), false);
    }
}
