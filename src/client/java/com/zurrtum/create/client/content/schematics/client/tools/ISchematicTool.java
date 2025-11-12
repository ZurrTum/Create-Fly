package com.zurrtum.create.client.content.schematics.client.tools;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;

public interface ISchematicTool {

    void init();

    void updateSelection(Minecraft mc);

    boolean handleRightClick(Minecraft mc);

    boolean handleMouseWheel(double delta);

    void renderTool(Minecraft mc, PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera);

    void renderOverlay(Gui gui, GuiGraphics graphics, float partialTicks, int width, int height);

    void renderOnSchematic(Minecraft mc, PoseStack ms, SuperRenderTypeBuffer buffer);

}
