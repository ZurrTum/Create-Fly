package com.zurrtum.create.client.compat.rei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.rei.CreateCategory;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.SandPaperRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.compat.rei.display.SandpaperPolishingDisplay;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2f;

import java.util.List;

public class SandpaperPolishingCategory extends CreateCategory<SandpaperPolishingDisplay> {
    @Override
    public CategoryIdentifier<? extends SandpaperPolishingDisplay> getCategoryIdentifier() {
        return ReiCommonPlugin.SANDPAPER_POLISHING;
    }

    @Override
    public Component getTitle() {
        return CreateLang.translateDirect("recipe.sandpaper_polishing");
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(AllItems.SAND_PAPER);
    }

    @Override
    public void addWidgets(List<Widget> widgets, SandpaperPolishingDisplay display, Rectangle bounds) {
        Point input = new Point(bounds.x + 32, bounds.y + 34);
        Point output = new Point(bounds.x + 137, bounds.y + 34);
        Slot inputSlot = createInputSlot(input).entries(display.input());
        widgets.add(Widgets.createDrawableWidget((GuiGraphics graphics, int mouseX, int mouseY, float delta) -> {
            drawSlotBackground(graphics, input, output);
            AllGuiTextures.JEI_SHADOW.render(graphics, bounds.x + 66, bounds.y + 26);
            AllGuiTextures.JEI_LONG_ARROW.render(graphics, bounds.x + 57, bounds.y + 37);
            graphics.guiRenderState.submitPicturesInPictureState(new SandPaperRenderState(
                new Matrix3x2f(graphics.pose()),
                inputSlot.getCurrentEntry().castValue(),
                bounds.x + 79,
                bounds.y + 3
            ));
        }));
        widgets.add(inputSlot);
        widgets.add(createOutputSlot(output).entries(display.output()));
    }

    @Override
    public int getDisplayHeight() {
        return 65;
    }
}
