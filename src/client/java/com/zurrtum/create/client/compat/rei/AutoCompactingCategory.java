package com.zurrtum.create.client.compat.rei;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.PressBasinRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.plugin.common.displays.crafting.CraftingDisplay;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class AutoCompactingCategory implements DisplayCategory<CraftingDisplay> {
    @Override
    public CategoryIdentifier<? extends CraftingDisplay> getCategoryIdentifier() {
        return ReiCommonPlugin.AUTO_COMPACTING;
    }

    @Override
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.automatic_packing");
    }

    @Override
    public Renderer getIcon() {
        return new TwoIconRenderer(AllItems.MECHANICAL_PRESS.getDefaultStack(), Items.CRAFTING_TABLE.getDefaultStack());
    }

    @Override
    public List<Widget> setupDisplay(CraftingDisplay display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));
        List<EntryIngredient> ingredients = display.getInputEntries().stream().filter(e -> !e.isEmpty()).toList();
        List<Point> points = new ArrayList<>();
        for (int i = 0, size = ingredients.size(), rows = size == 4 ? 2 : 3; i < size; i++) {
            points.add(new Point(bounds.x + 5 + (rows == 2 ? 27 : 18) + (i % rows) * 19, bounds.y + 56 - (i / rows) * 19));
        }
        Point output = new Point(bounds.x + 147, bounds.y + 56);
        widgets.add(Widgets.createDrawableWidget((DrawContext graphics, int mouseX, int mouseY, float delta) -> {
            for (Point point : points) {
                AllGuiTextures.JEI_SLOT.render(graphics, point.x - 1, point.y - 1);
            }
            AllGuiTextures.JEI_SLOT.render(graphics, output.x - 1, output.y - 1);
            AllGuiTextures.JEI_DOWN_ARROW.render(graphics, bounds.x + 141, bounds.y + 37);
            AllGuiTextures.JEI_SHADOW.render(graphics, bounds.x + 86, bounds.y + 73);
            graphics.state.addSpecialElement(new PressBasinRenderState(new Matrix3x2f(graphics.getMatrices()), bounds.x + 96, bounds.y));
        }));
        for (int i = 0, size = points.size(); i < size; i++) {
            widgets.add(Widgets.createSlot(points.get(i)).markInput().disableBackground().entries(ingredients.get(i)));
        }
        widgets.add(Widgets.createSlot(output).markOutput().disableBackground().entries(display.getOutputEntries().getFirst()));
        return widgets;
    }

    @Override
    public int getDisplayWidth(CraftingDisplay display) {
        return 187;
    }

    @Override
    public int getDisplayHeight() {
        return 95;
    }
}
