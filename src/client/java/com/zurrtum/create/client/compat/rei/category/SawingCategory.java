package com.zurrtum.create.client.compat.rei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.rei.CreateCategory;
import com.zurrtum.create.client.compat.rei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.SawRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.compat.rei.display.SawingDisplay;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class SawingCategory extends CreateCategory<SawingDisplay> {
    @Override
    public CategoryIdentifier<? extends SawingDisplay> getCategoryIdentifier() {
        return ReiCommonPlugin.SAWING;
    }

    @Override
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.sawing");
    }

    @Override
    public Renderer getIcon() {
        return new TwoIconRenderer(AllItems.MECHANICAL_SAW, Items.OAK_LOG);
    }

    @Override
    public void addWidgets(List<Widget> widgets, SawingDisplay display, Rectangle bounds) {
        Point input = new Point(bounds.x + 49, bounds.y + 10);
        List<Point> outputs = new ArrayList<>();
        List<EntryIngredient> outputIngredients = new ArrayList<>();
        List<Point> chances = new ArrayList<>();
        List<EntryIngredient> chanceIngredients = new ArrayList<>();
        List<ProcessingOutput> results = display.outputs();
        for (int i = 0, size = results.size(), start = bounds.x + 123, y = bounds.y + 53; i < size; i++) {
            addOutputData(results.get(i), i % 2 == 0 ? start : start + 19, y + (i / 2) * -19, outputs, outputIngredients, chances, chanceIngredients);
        }
        widgets.add(Widgets.createDrawableWidget((DrawContext graphics, int mouseX, int mouseY, float delta) -> {
            drawSlotBackground(graphics, outputs, input);
            drawChanceSlotBackground(graphics, chances);
            AllGuiTextures.JEI_DOWN_ARROW.render(graphics, bounds.x + 75, bounds.y + 11);
            AllGuiTextures.JEI_SHADOW.render(graphics, bounds.x + 60, bounds.y + 60);
            graphics.state.addSpecialElement(new SawRenderState(new Matrix3x2f(graphics.getMatrices()), bounds.x + 69, bounds.y + 36));
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
