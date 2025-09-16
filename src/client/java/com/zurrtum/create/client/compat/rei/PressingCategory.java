package com.zurrtum.create.client.compat.rei;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.PressRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.rei.PressingDisplay;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class PressingCategory implements DisplayCategory<PressingDisplay> {
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
        return new TwoIconRenderer(AllItems.MECHANICAL_PRESS.getDefaultStack(), AllItems.IRON_SHEET.getDefaultStack());
    }

    @Override
    public List<Widget> setupDisplay(PressingDisplay display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));
        Point input = new Point(bounds.x + 32, bounds.y + 56);
        Point output = new Point(bounds.x + 136, bounds.y + 56);
        widgets.add(Widgets.createDrawableWidget((DrawContext graphics, int mouseX, int mouseY, float delta) -> {
            AllGuiTextures.JEI_SLOT.render(graphics, input.x - 1, input.y - 1);
            AllGuiTextures.JEI_SLOT.render(graphics, output.x - 1, output.y - 1);
            AllGuiTextures.JEI_SHADOW.render(graphics, bounds.x + 66, bounds.y + 46);
            AllGuiTextures.JEI_LONG_ARROW.render(graphics, bounds.x + 57, bounds.y + 59);
            graphics.state.addSpecialElement(new PressRenderState(new Matrix3x2f(graphics.getMatrices()), bounds.x + 78, bounds.y - 11));
        }));
        widgets.add(Widgets.createSlot(input).markInput().disableBackground().entries(display.input()));
        widgets.add(Widgets.createSlot(output).markOutput().disableBackground().entries(display.output()));
        return widgets;
    }

    @Override
    public int getDisplayWidth(PressingDisplay display) {
        return 187;
    }

    @Override
    public int getDisplayHeight() {
        return 80;
    }
}
