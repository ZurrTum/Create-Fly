package com.zurrtum.create.client.compat.rei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.rei.CreateCategory;
import com.zurrtum.create.client.compat.rei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.PressRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.compat.rei.display.PressingDisplay;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.joml.Matrix3x2f;

import java.util.List;

public class PressingCategory extends CreateCategory<PressingDisplay> {
    @Override
    public CategoryIdentifier<? extends PressingDisplay> getCategoryIdentifier() {
        return ReiCommonPlugin.PRESSING;
    }

    @Override
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.pressing");
    }

    @Override
    public Renderer getIcon() {
        return new TwoIconRenderer(AllItems.MECHANICAL_PRESS, AllItems.IRON_SHEET);
    }

    @Override
    public void addWidgets(List<Widget> widgets, PressingDisplay display, Rectangle bounds) {
        Point input = new Point(bounds.x + 32, bounds.y + 56);
        Point output = new Point(bounds.x + 136, bounds.y + 56);
        widgets.add(Widgets.createDrawableWidget((DrawContext graphics, int mouseX, int mouseY, float delta) -> {
            drawSlotBackground(graphics, input, output);
            AllGuiTextures.JEI_SHADOW.render(graphics, bounds.x + 66, bounds.y + 46);
            AllGuiTextures.JEI_LONG_ARROW.render(graphics, bounds.x + 57, bounds.y + 59);
            graphics.state.addSpecialElement(new PressRenderState(new Matrix3x2f(graphics.getMatrices()), bounds.x + 78, bounds.y - 11));
        }));
        widgets.add(createInputSlot(input).entries(display.input()));
        widgets.add(createOutputSlot(output).entries(display.output()));
    }

    @Override
    public int getDisplayHeight() {
        return 80;
    }
}
