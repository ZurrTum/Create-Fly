package com.zurrtum.create.client.compat.rei;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.MixingBasinRenderState;
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

public class AutoMixingCategory extends CreateCategory<CraftingDisplay> {
    @Override
    public CategoryIdentifier<? extends CraftingDisplay> getCategoryIdentifier() {
        return ReiCommonPlugin.AUTOMATIC_SHAPELESS;
    }

    @Override
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.automatic_shapeless");
    }

    @Override
    public Renderer getIcon() {
        return new TwoIconRenderer(AllItems.MECHANICAL_MIXER, Items.CRAFTING_TABLE);
    }

    @Override
    void addWidgets(List<Widget> widgets, CraftingDisplay display, Rectangle bounds) {
        List<EntryIngredient> ingredients = condenseIngredients(display.getInputEntries());
        List<Point> points = new ArrayList<>();
        for (int i = 0, size = ingredients.size(), xOffset = size < 3 ? (3 - size) * 19 / 2 : 0; i < size; i++) {
            points.add(new Point(bounds.x + 17 + xOffset + (i % 3) * 19, bounds.y + 56 - (i / 3) * 19));
        }
        Point output = new Point(bounds.x + 147, bounds.y + 56);
        widgets.add(Widgets.createDrawableWidget((DrawContext graphics, int mouseX, int mouseY, float delta) -> {
            drawSlotBackground(graphics, points, output);
            AllGuiTextures.JEI_DOWN_ARROW.render(graphics, bounds.x + 141, bounds.y + 37);
            AllGuiTextures.JEI_SHADOW.render(graphics, bounds.x + 86, bounds.y + 73);
            graphics.state.addSpecialElement(new MixingBasinRenderState(new Matrix3x2f(graphics.getMatrices()), bounds.x + 96, bounds.y));
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
