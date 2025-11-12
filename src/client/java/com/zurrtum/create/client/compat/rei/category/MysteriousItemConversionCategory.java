package com.zurrtum.create.client.compat.rei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.rei.CreateCategory;
import com.zurrtum.create.client.compat.rei.ReiClientPlugin;
import com.zurrtum.create.client.compat.rei.display.MysteriousItemConversionDisplay;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public class MysteriousItemConversionCategory extends CreateCategory<MysteriousItemConversionDisplay> {
    @Override
    public void addWidgets(List<Widget> widgets, MysteriousItemConversionDisplay display, Rectangle bounds) {
        Point input = new Point(bounds.x + 32, bounds.y + 22);
        Point output = new Point(bounds.x + 137, bounds.y + 22);
        widgets.add(Widgets.createDrawableWidget((GuiGraphics graphics, int mouseX, int mouseY, float delta) -> {
            drawSlotBackground(graphics, input, output);
            AllGuiTextures.JEI_LONG_ARROW.render(graphics, bounds.x + 57, bounds.y + 25);
            AllGuiTextures.JEI_QUESTION_MARK.render(graphics, bounds.x + 82, bounds.y + 10);
        }));
        widgets.add(createInputSlot(input).entries(display.input()));
        widgets.add(createOutputSlot(output).entries(display.output()));
    }

    @Override
    public CategoryIdentifier<? extends MysteriousItemConversionDisplay> getCategoryIdentifier() {
        return ReiClientPlugin.MYSTERY_CONVERSION;
    }

    @Override
    public Component getTitle() {
        return CreateLang.translateDirect("recipe.mystery_conversion");
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(AllItems.PECULIAR_BELL);
    }

    @Override
    public int getDisplayHeight() {
        return 60;
    }
}
