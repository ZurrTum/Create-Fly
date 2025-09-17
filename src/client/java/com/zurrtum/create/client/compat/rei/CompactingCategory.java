package com.zurrtum.create.client.compat.rei;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.PressBasinRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.rei.CompactingDisplay;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class CompactingCategory extends CreateCategory<CompactingDisplay> {
    @Override
    public CategoryIdentifier<? extends CompactingDisplay> getCategoryIdentifier() {
        return ReiCommonPlugin.PACKING;
    }

    @Override
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.packing");
    }

    @Override
    public Renderer getIcon() {
        return new TwoIconRenderer(AllItems.MECHANICAL_PRESS, AllItems.BASIN);
    }

    @Override
    void addWidgets(List<Widget> widgets, CompactingDisplay display, Rectangle bounds) {
        List<EntryIngredient> ingredients = display.inputs();
        List<Point> points = new ArrayList<>();
        for (int i = 0, size = ingredients.size(), xOffset = size < 3 ? (3 - size) * 19 / 2 : 0; i < size; i++) {
            points.add(new Point(bounds.x + 17 + xOffset + (i % 3) * 19, bounds.y + 56 - (i / 3) * 19));
        }
        Point output = new Point(bounds.x + 147, bounds.y + 56);
        widgets.add(Widgets.createDrawableWidget((DrawContext graphics, int mouseX, int mouseY, float delta) -> {
            drawSlotBackground(graphics, points);
            AllGuiTextures.JEI_SLOT.render(graphics, output.x - 1, output.y - 1);
            AllGuiTextures.JEI_DOWN_ARROW.render(graphics, bounds.x + 141, bounds.y + 37);
            AllGuiTextures.JEI_SHADOW.render(graphics, bounds.x + 86, bounds.y + 73);
            graphics.state.addSpecialElement(new PressBasinRenderState(new Matrix3x2f(graphics.getMatrices()), bounds.x + 96, bounds.y));
        }));
        for (int i = 0, size = points.size(); i < size; i++) {
            widgets.add(createInputSlot(points.get(i)).entries(getRenderEntryStack(ingredients.get(i))));
        }
        widgets.add(createOutputSlot(output).entries(display.output()));
    }

    @Override
    public int getDisplayHeight() {
        return 95;
    }
}