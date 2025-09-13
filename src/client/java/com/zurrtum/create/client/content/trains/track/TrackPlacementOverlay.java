package com.zurrtum.create.client.content.trains.track;

import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.trains.track.TrackPlacement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

public class TrackPlacementOverlay {
    public static void render(MinecraftClient mc, DrawContext guiGraphics) {
        if (TrackPlacement.hoveringPos == null)
            return;
        if (TrackPlacement.cached == null || TrackPlacement.cached.curve == null || !TrackPlacement.cached.valid)
            return;
        if (TrackPlacementClient.extraTipWarmup < 4)
            return;

        if (mc.inGameHud.heldItemTooltipFade > 0)
            return;

        boolean active = mc.options.sprintKey.isPressed();
        MutableText text = CreateLang.translateDirect(
            "track.hold_for_smooth_curve",
            Text.keybind("key.sprint").formatted(active ? Formatting.WHITE : Formatting.GRAY)
        );

        Window window = mc.getWindow();
        int x = (window.getScaledWidth() - mc.textRenderer.getWidth(text)) / 2;
        int y = window.getScaledHeight() - 61;
        Color color = new Color(0x4ADB4A).setAlpha(MathHelper.clamp((TrackPlacementClient.extraTipWarmup - 4) / 3f, 0.1f, 1));
        guiGraphics.drawText(mc.textRenderer, text, x, y, color.getRGB(), false);
    }
}
