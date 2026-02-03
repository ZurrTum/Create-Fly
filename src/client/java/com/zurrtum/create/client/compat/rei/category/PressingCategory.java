package com.zurrtum.create.client.compat.rei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.rei.CreateCategory;
import com.zurrtum.create.client.compat.rei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.PressRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.compat.rei.display.PressingDisplay;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class PressingCategory extends CreateCategory<PressingDisplay> {
    @Override
    public CategoryIdentifier<? extends PressingDisplay> getCategoryIdentifier() {
        return ReiCommonPlugin.PRESSING;
    }

    @Override
    public Component getTitle() {
        return CreateLang.translateDirect("recipe.pressing");
    }

    @Override
    public Renderer getIcon() {
        return new TwoIconRenderer(AllItems.MECHANICAL_PRESS, AllItems.IRON_SHEET);
    }

    @Override
    public void addWidgets(List<Widget> widgets, PressingDisplay display, Rectangle bounds) {
        Point input = new Point(bounds.x + 32, bounds.y + 56);
        List<Point> outputs = new ArrayList<>();
        List<EntryIngredient> outputIngredients = new ArrayList<>();
        List<Point> chances = new ArrayList<>();
        List<EntryIngredient> chanceIngredients = new ArrayList<>();
        List<ProcessingOutput> results = display.outputs();
        for (int i = 0, size = results.size(), start = bounds.x + 136, y = bounds.y + 56; i < size; i++) {
            addOutputData(results.get(i), start + 19 * i, y, outputs, outputIngredients, chances, chanceIngredients);
        }
        widgets.add(Widgets.createDrawableWidget((GuiGraphics graphics, int mouseX, int mouseY, float delta) -> {
            drawSlotBackground(graphics, outputs, input);
            drawChanceSlotBackground(graphics, chances);
            AllGuiTextures.JEI_SHADOW.render(graphics, bounds.x + 66, bounds.y + 46);
            AllGuiTextures.JEI_LONG_ARROW.render(graphics, bounds.x + 57, bounds.y + 59);
            graphics.guiRenderState.submitPicturesInPictureState(new PressRenderState(new Matrix3x2f(graphics.pose()), bounds.x + 78, bounds.y - 11));
        }));
        widgets.add(createInputSlot(input).entries(display.input()));
        for (int i = 0, size = outputs.size(); i < size; i++) {
            widgets.add(createOutputSlot(outputs.get(i)).entries(outputIngredients.get(i)));
        }
        for (int i = 0, size = chances.size(); i < size; i++) {
            widgets.add(createOutputSlot(chances.get(i)).entries(chanceIngredients.get(i)));
        }
    }

    @Override
    public int getDisplayHeight() {
        return 80;
    }
}
