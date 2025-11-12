package com.zurrtum.create.client.content.schematics.client;

import com.mojang.blaze3d.platform.Window;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

public class SchematicHotbarSlotOverlay {

    public void renderOn(Minecraft mc, GuiGraphics graphics, int slot, float tickProgress) {
        Window mainWindow = mc.getWindow();
        int x = (mainWindow.getGuiScaledWidth() / 2 - 88) + 20 * slot;
        int y = mainWindow.getGuiScaledHeight() - 19;
        AllGuiTextures.SCHEMATIC_SLOT.render(graphics, x, y);
        ItemStack stack = mc.player.getInventory().getItem(slot);
        float f = stack.getPopTime() - tickProgress;
        Matrix3x2fStack ms = graphics.pose();
        if (f > 0.0F) {
            float g = 1.0F + f / 5.0F;
            ms.pushMatrix();
            ms.translate(x + 8, y + 12);
            ms.scale(1.0F / g, (g + 1.0F) / 2.0F);
            ms.translate(-(x + 8), -(y + 12));
        }
        graphics.renderItem(mc.player, stack, x, y, slot + 1);
        if (f > 0.0F) {
            ms.popMatrix();
        }
        graphics.renderItemDecorations(mc.font, stack, x, y);
    }

}
