package com.zurrtum.create.client.foundation.gui;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.catnip.gui.UIRenderHelper;
import com.zurrtum.create.client.foundation.gui.widget.ScrollInput;
import com.zurrtum.create.client.foundation.gui.widget.TooltipArea;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ModularGuiLine {

    List<Pair<ClickableWidget, String>> widgets;
    List<Couple<Integer>> customBoxes;
    boolean speechBubble;

    public ModularGuiLine() {
        widgets = new ArrayList<>();
        customBoxes = new ArrayList<>();
        speechBubble = false;
    }

    public void renderWidgetBG(int guiLeft, DrawContext graphics) {
        boolean first = true;

        if (!customBoxes.isEmpty()) {
            for (Couple<Integer> couple : customBoxes) {
                int x = couple.getFirst() + guiLeft;
                int width = couple.getSecond();
                box(graphics, x, width, first & speechBubble);
                first = false;
            }
            return;
        }

        for (Pair<ClickableWidget, String> pair : widgets) {
            if (pair.getSecond().equals("Dummy"))
                continue;

            ClickableWidget aw = pair.getFirst();
            int x = aw.getX();
            int width = aw.getWidth();

            if (aw instanceof TextFieldWidget) {
                x -= 5;
                width += 9;
            }

            box(graphics, x, width, first & speechBubble);
            first = false;
        }
    }

    private void box(DrawContext graphics, int x, int width, boolean b) {
        UIRenderHelper.drawStretched(graphics, x, 0, width, 18, AllGuiTextures.DATA_AREA);
        if (b)
            AllGuiTextures.DATA_AREA_SPEECH.render(graphics, x - 3, 0);
        else
            AllGuiTextures.DATA_AREA_START.render(graphics, x, 0);
        AllGuiTextures.DATA_AREA_END.render(graphics, x + width - 2, 0);
    }

    public void saveValues(NbtCompound data) {
        for (Pair<ClickableWidget, String> pair : widgets) {
            ClickableWidget w = pair.getFirst();
            String key = pair.getSecond();
            if (w instanceof TextFieldWidget eb)
                data.putString(key, eb.getText());
            if (w instanceof ScrollInput si)
                data.putInt(key, si.getState());
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Element & Drawable & Selectable> void loadValues(NbtCompound data, Consumer<T> addRenderable, Consumer<T> addRenderableOnly) {
        for (Pair<ClickableWidget, String> pair : widgets) {
            ClickableWidget w = pair.getFirst();
            String key = pair.getSecond();
            if (w instanceof TextFieldWidget eb)
                eb.setText(data.getString(key, ""));
            if (w instanceof ScrollInput si)
                si.setState(data.getInt(key, 0));

            if (w instanceof TooltipArea)
                addRenderableOnly.accept((T) w);
            else
                addRenderable.accept((T) w);
        }
    }

    public void forEach(Consumer<Element> callback) {
        widgets.forEach(p -> callback.accept(p.getFirst()));
    }

    public void clear() {
        widgets.clear();
        customBoxes.clear();
    }

    public void add(Pair<ClickableWidget, String> pair) {
        widgets.add(pair);
    }

}
