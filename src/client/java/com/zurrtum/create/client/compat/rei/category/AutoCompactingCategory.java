package com.zurrtum.create.client.compat.rei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.rei.CreateCategory;
import com.zurrtum.create.client.compat.rei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.PressBasinRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.plugin.common.displays.crafting.CraftingDisplay;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class AutoCompactingCategory extends CreateCategory<CraftingDisplay> {
    @Override
    public CategoryIdentifier<? extends CraftingDisplay> getCategoryIdentifier() {
        return ReiCommonPlugin.AUTOMATIC_PACKING;
    }

    @Override
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.automatic_packing");
    }

    @Override
    public Renderer getIcon() {
        return new TwoIconRenderer(AllItems.MECHANICAL_PRESS, Items.CRAFTING_TABLE);
    }

    @Override
    public void addWidgets(List<Widget> widgets, CraftingDisplay display, Rectangle bounds) {
        List<EntryIngredient> ingredients = display.getInputEntries().stream().filter(e -> !e.isEmpty()).toList();
        List<Point> points = new ArrayList<>();
        for (int i = 0, size = ingredients.size(), rows = size == 4 ? 2 : 3; i < size; i++) {
            points.add(new Point(bounds.x + 5 + (rows == 2 ? 27 : 18) + (i % rows) * 19, bounds.y + 56 - (i / rows) * 19));
        }
        Point output = new Point(bounds.x + 147, bounds.y + 56);
        widgets.add(Widgets.createDrawableWidget((DrawContext graphics, int mouseX, int mouseY, float delta) -> {
            drawSlotBackground(graphics, points, output);
            AllGuiTextures.JEI_DOWN_ARROW.render(graphics, bounds.x + 141, bounds.y + 37);
            AllGuiTextures.JEI_SHADOW.render(graphics, bounds.x + 86, bounds.y + 73);
            graphics.state.addSpecialElement(new PressBasinRenderState(new Matrix3x2f(graphics.getMatrices()), bounds.x + 96, bounds.y));
        }));
        for (int i = 0, size = points.size(); i < size; i++) {
            widgets.add(createInputSlot(points.get(i)).entries(ingredients.get(i)));
        }
        widgets.add(createOutputSlot(output).entries(display.getOutputEntries().getFirst()));
    }

    @Override
    public int getDisplayHeight() {
        return 95;
    }
}
