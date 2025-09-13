package com.zurrtum.create.client.foundation.gui.widget;

import com.zurrtum.create.client.catnip.gui.widget.AbstractSimiWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class Label extends AbstractSimiWidget {

    public Text text;
    public String suffix;
    protected boolean hasShadow;
    protected int color;
    protected TextRenderer font;

    public Label(int x, int y, Text text) {
        super(x, y, MinecraftClient.getInstance().textRenderer.getWidth(text), 10);
        font = MinecraftClient.getInstance().textRenderer;
        this.text = Text.literal("Label");
        color = 0xFFFFFFFF;
        hasShadow = false;
        suffix = "";
    }

    public Label colored(int color) {
        this.color = color;
        return this;
    }

    public Label withShadow() {
        this.hasShadow = true;
        return this;
    }

    public Label withSuffix(String s) {
        suffix = s;
        return this;
    }

    public void setTextAndTrim(Text newText, boolean trimFront, int maxWidthPx) {
        TextRenderer fontRenderer = MinecraftClient.getInstance().textRenderer;

        if (fontRenderer.getWidth(newText) <= maxWidthPx) {
            text = newText;
            return;
        }

        String trim = "...";
        int trimWidth = fontRenderer.getWidth(trim);

        String raw = newText.getString();
        StringBuilder builder = new StringBuilder(raw);
        int startIndex = trimFront ? 0 : raw.length() - 1;
        int endIndex = !trimFront ? 0 : raw.length() - 1;
        int step = (int) Math.signum(endIndex - startIndex);

        for (int i = startIndex; i != endIndex; i += step) {
            String sub = builder.substring(trimFront ? i : startIndex, trimFront ? endIndex + 1 : i + 1);
            if (fontRenderer.getWidth(Text.literal(sub).setStyle(newText.getStyle())) + trimWidth <= maxWidthPx) {
                text = Text.literal(trimFront ? trim + sub : sub + trim).setStyle(newText.getStyle());
                return;
            }
        }

    }

    @Override
    protected void doRender(@NotNull DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        if (text == null || text.getString().isEmpty())
            return;

        MutableText copy = text.copyContentOnly();
        if (suffix != null && !suffix.isEmpty())
            copy.append(suffix);

        graphics.drawText(font, copy, getX(), getY(), color, hasShadow);
    }

}
