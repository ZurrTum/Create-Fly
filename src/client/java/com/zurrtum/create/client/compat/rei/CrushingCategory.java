package com.zurrtum.create.client.compat.rei;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.CrushWheelRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.rei.CrushingDisplay;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.processing.recipe.ChanceOutput;
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

public class CrushingCategory extends CreateCategory<CrushingDisplay> {
    @Override
    public CategoryIdentifier<? extends CrushingDisplay> getCategoryIdentifier() {
        return ReiCommonPlugin.CRUSHING;
    }

    @Override
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.crushing");
    }

    @Override
    public Renderer getIcon() {
        return new TwoIconRenderer(AllItems.CRUSHING_WHEEL, AllItems.CRUSHED_GOLD);
    }

    @Override
    void addWidgets(List<Widget> widgets, CrushingDisplay display, Rectangle bounds) {
        Point input = new Point(bounds.x + 56, bounds.y + 8);
        List<Point> outputs = new ArrayList<>();
        List<EntryIngredient> outputIngredients = new ArrayList<>();
        List<Point> chances = new ArrayList<>();
        List<EntryIngredient> chanceIngredients = new ArrayList<>();
        List<ChanceOutput> results = display.outputs();
        for (int i = 0, size = results.size(), start = bounds.x + (184 - 19 * size) / 2 + 3, y = bounds.y + 83; i < size; i++) {
            ChanceOutput output = results.get(i);
            addOutputData(output, start + i * 19, y, outputs, outputIngredients, chances, chanceIngredients);
        }
        widgets.add(Widgets.createDrawableWidget((DrawContext graphics, int mouseX, int mouseY, float delta) -> {
            drawSlotBackground(graphics, outputs, input);
            drawChanceSlotBackground(graphics, chances);
            AllGuiTextures.JEI_DOWN_ARROW.render(graphics, bounds.x + 77, bounds.y + 12);
            graphics.state.addSpecialElement(new CrushWheelRenderState(new Matrix3x2f(graphics.getMatrices()), bounds.x + 47, bounds.y + 29));
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
        return 110;
    }
}
