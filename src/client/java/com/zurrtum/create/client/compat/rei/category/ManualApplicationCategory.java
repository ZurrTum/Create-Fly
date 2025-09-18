package com.zurrtum.create.client.compat.rei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.rei.CreateCategory;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.compat.rei.display.ManualApplicationDisplay;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.List;

public class ManualApplicationCategory extends CreateCategory<ManualApplicationDisplay> {
    @Override
    public CategoryIdentifier<? extends ManualApplicationDisplay> getCategoryIdentifier() {
        return ReiCommonPlugin.ITEM_APPLICATION;
    }

    @Override
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.item_application");
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(AllItems.BRASS_HAND);
    }

    @Override
    public void addWidgets(List<Widget> widgets, ManualApplicationDisplay display, Rectangle bounds) {
        Point input = new Point(bounds.x + 56, bounds.y + 10);
        Point target = new Point(bounds.x + 32, bounds.y + 43);
        Point output = new Point(bounds.x + 137, bounds.y + 43);
        widgets.add(Widgets.createDrawableWidget((DrawContext graphics, int mouseX, int mouseY, float delta) -> {
            drawSlotBackground(graphics, input, target, output);
            AllGuiTextures.JEI_SHADOW.render(graphics, bounds.x + 67, bounds.y + 52);
            AllGuiTextures.JEI_DOWN_ARROW.render(graphics, bounds.x + 79, bounds.y + 15);
        }));
        widgets.add(createInputSlot(input).entries(display.input()));
        widgets.add(createInputSlot(target).entries(display.target()));
        widgets.add(createOutputSlot(output).entries(display.output()));
    }

    @Override
    public int getDisplayHeight() {
        return 70;
    }
}
