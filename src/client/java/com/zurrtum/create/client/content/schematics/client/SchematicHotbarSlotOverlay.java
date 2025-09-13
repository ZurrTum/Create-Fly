package com.zurrtum.create.client.content.schematics.client;

import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.item.ItemStack;
import org.joml.Matrix3x2fStack;

public class SchematicHotbarSlotOverlay {

    public void renderOn(MinecraftClient mc, DrawContext graphics, int slot, float tickProgress) {
        Window mainWindow = mc.getWindow();
        int x = (mainWindow.getScaledWidth() / 2 - 88) + 20 * slot;
        int y = mainWindow.getScaledHeight() - 19;
        AllGuiTextures.SCHEMATIC_SLOT.render(graphics, x, y);
        ItemStack stack = mc.player.getInventory().getStack(slot);
        float f = stack.getBobbingAnimationTime() - tickProgress;
        Matrix3x2fStack ms = graphics.getMatrices();
        if (f > 0.0F) {
            float g = 1.0F + f / 5.0F;
            ms.pushMatrix();
            ms.translate(x + 8, y + 12);
            ms.scale(1.0F / g, (g + 1.0F) / 2.0F);
            ms.translate(-(x + 8), -(y + 12));
        }
        graphics.drawItem(mc.player, stack, x, y, slot + 1);
        if (f > 0.0F) {
            ms.popMatrix();
        }
        graphics.drawStackOverlay(mc.textRenderer, stack, x, y);
    }

}
