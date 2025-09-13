package com.zurrtum.create.client.content.schematics.client.tools;

import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

public interface ISchematicTool {

    void init();

    void updateSelection(MinecraftClient mc);

    boolean handleRightClick(MinecraftClient mc);

    boolean handleMouseWheel(double delta);

    void renderTool(MinecraftClient mc, MatrixStack ms, SuperRenderTypeBuffer buffer, Vec3d camera);

    void renderOverlay(InGameHud gui, DrawContext graphics, float partialTicks, int width, int height);

    void renderOnSchematic(MinecraftClient mc, MatrixStack ms, SuperRenderTypeBuffer buffer);

}
