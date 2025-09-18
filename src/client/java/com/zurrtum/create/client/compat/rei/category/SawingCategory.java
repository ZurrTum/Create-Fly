package com.zurrtum.create.client.compat.rei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.rei.CreateCategory;
import com.zurrtum.create.client.compat.rei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.SawRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.compat.rei.display.SawingDisplay;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.joml.Matrix3x2f;

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
        Point output = new Point(bounds.x + 123, bounds.y + 53);
        widgets.add(Widgets.createDrawableWidget((DrawContext graphics, int mouseX, int mouseY, float delta) -> {
            drawSlotBackground(graphics, input, output);
            AllGuiTextures.JEI_DOWN_ARROW.render(graphics, bounds.x + 75, bounds.y + 11);
            AllGuiTextures.JEI_SHADOW.render(graphics, bounds.x + 60, bounds.y + 60);
            graphics.state.addSpecialElement(new SawRenderState(new Matrix3x2f(graphics.getMatrices()), bounds.x + 69, bounds.y + 36));
        }));
        widgets.add(createInputSlot(input).entries(display.input()));
        widgets.add(createOutputSlot(output).entries(display.output()));
    }

    @Override
    public int getDisplayHeight() {
        return 80;
    }
}
