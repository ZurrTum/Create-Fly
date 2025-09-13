package com.zurrtum.create.client.content.schematics.client;

import com.zurrtum.create.client.AllKeys;
import com.zurrtum.create.client.content.schematics.client.tools.ToolType;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

import java.util.List;
import java.util.function.Consumer;

public class ToolSelectionScreen extends Screen {

    public final String scrollToCycle = CreateLang.translateDirect("gui.toolmenu.cycle").getString();
    public final String holdToFocus = "gui.toolmenu.focusKey";

    protected List<ToolType> tools;
    protected Consumer<ToolType> callback;
    public boolean focused;
    private float yOffset;
    protected int selection;
    private boolean initialized;

    protected int w;
    protected int h;

    public ToolSelectionScreen(MinecraftClient mc, List<ToolType> tools, Consumer<ToolType> callback) {
        super(Text.literal("Tool Selection"));
        this.client = mc;
        this.tools = tools;
        this.callback = callback;
        focused = false;
        yOffset = 0;
        selection = 0;
        initialized = false;

        callback.accept(tools.get(selection));

        w = Math.max(tools.size() * 50 + 30, 220);
        h = 30;
    }

    public void setSelectedElement(ToolType tool) {
        if (!tools.contains(tool))
            return;
        selection = tools.indexOf(tool);
    }

    public void cycle(int direction) {
        selection += (direction < 0) ? 1 : -1;
        selection = (selection + tools.size()) % tools.size();
    }

    private void draw(DrawContext graphics, float partialTicks) {
        Matrix3x2fStack matrixStack = graphics.getMatrices();
        Window mainWindow = client.getWindow();
        int scaledWidth = mainWindow.getScaledWidth();
        int scaledHeight = mainWindow.getScaledHeight();
        if (!initialized)
            init(client, scaledWidth, scaledHeight);

        int x = (scaledWidth - w) / 2 + 15;
        int y = scaledHeight - h - 75;

        matrixStack.pushMatrix();
        matrixStack.translate(0, -yOffset);

        AllGuiTextures gray = AllGuiTextures.HUD_BACKGROUND;

        graphics.drawTexture(
            RenderPipelines.GUI_TEXTURED,
            gray.location,
            x - 15,
            y,
            gray.getStartX(),
            gray.getStartY(),
            w,
            h,
            gray.getWidth(),
            gray.getHeight(),
            focused ? 0xE0FFFFFF : 0x80FFFFFF
        );

        float toolTipAlpha = yOffset / 10;
        List<Text> toolTip = tools.get(selection).getDescription();
        int stringAlphaComponent = ((int) (toolTipAlpha * 0xFF)) << 24;

        if (toolTipAlpha > 0.25f) {
            graphics.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                gray.location,
                x - 15,
                y + 33,
                gray.getStartX(),
                gray.getStartY(),
                w,
                h + 22,
                gray.getWidth(),
                gray.getHeight(),
                0xB2B2CC | stringAlphaComponent
            );

            if (toolTip.size() > 0)
                graphics.drawText(textRenderer, toolTip.get(0), x - 10, y + 38, 0xEEEEEE | stringAlphaComponent, false);
            if (toolTip.size() > 1)
                graphics.drawText(textRenderer, toolTip.get(1), x - 10, y + 50, 0xCCDDFF | stringAlphaComponent, false);
            if (toolTip.size() > 2)
                graphics.drawText(textRenderer, toolTip.get(2), x - 10, y + 60, 0xCCDDFF | stringAlphaComponent, false);
            if (toolTip.size() > 3)
                graphics.drawText(textRenderer, toolTip.get(3), x - 10, y + 72, 0xCCCCDD | stringAlphaComponent, false);
        }

        if (tools.size() > 1) {
            String keyName = AllKeys.TOOL_MENU.getBoundKeyLocalizedText().getString().toUpperCase();
            if (!focused)
                graphics.drawCenteredTextWithShadow(
                    textRenderer,
                    CreateLang.translateDirect(holdToFocus, keyName),
                    scaledWidth / 2,
                    y - 10,
                    0xFFCCDDFF
                );
            else
                graphics.drawCenteredTextWithShadow(textRenderer, scrollToCycle, scaledWidth / 2, y - 10, 0xFFCCDDFF);
        } else {
            x += 65;
        }


        for (int i = 0; i < tools.size(); i++) {
            matrixStack.pushMatrix();

            float alpha = focused ? 1 : .2f;
            if (i == selection) {
                matrixStack.translate(0, -10);
                graphics.drawCenteredTextWithShadow(textRenderer, tools.get(i).getDisplayName().getString(), x + i * 50 + 24, y + 28, 0xFFCCDDFF);
                alpha = 1;
            }
            int color = ((int) (alpha * 0xFF)) << 24;
            tools.get(i).getIcon().render(graphics, x + i * 50 + 16, y + 12, color);
            tools.get(i).getIcon().render(graphics, x + i * 50 + 16, y + 11, 0xFFFFFF | color);

            matrixStack.popMatrix();
        }

        matrixStack.popMatrix();
    }

    public void update() {
        if (focused)
            yOffset += (10 - yOffset) * .1f;
        else
            yOffset *= .9f;
    }

    public void renderPassive(DrawContext graphics, float partialTicks) {
        draw(graphics, partialTicks);
    }

    @Override
    public void close() {
        callback.accept(tools.get(selection));
    }

    @Override
    protected void init() {
        super.init();
        initialized = true;
    }
}