package com.zurrtum.create.client.compat.rei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.rei.CreateCategory;
import com.zurrtum.create.client.compat.rei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.DrainRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.compat.rei.display.DrainingDisplay;
import dev.architectury.fluid.FluidStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.joml.Matrix3x2f;

import java.util.List;

public class DrainingCategory extends CreateCategory<DrainingDisplay> {
    @Override
    public CategoryIdentifier<? extends DrainingDisplay> getCategoryIdentifier() {
        return ReiCommonPlugin.DRAINING;
    }

    @Override
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.draining");
    }

    @Override
    public Renderer getIcon() {
        return new TwoIconRenderer(AllItems.ITEM_DRAIN, Items.WATER_BUCKET);
    }

    @Override
    public void addWidgets(List<Widget> widgets, DrainingDisplay display, Rectangle bounds) {
        Point input = new Point(bounds.x + 32, bounds.y + 13);
        Point output = new Point(bounds.x + 137, bounds.y + 13);
        Point result = new Point(bounds.x + 137, bounds.y + 32);
        Slot fluidSlot = createOutputSlot(output).entries(getRenderEntryStack(display.output()));
        widgets.add(Widgets.createDrawableWidget((DrawContext graphics, int mouseX, int mouseY, float delta) -> {
            drawSlotBackground(graphics, input, output, result);
            AllGuiTextures.JEI_SHADOW.render(graphics, bounds.x + 67, bounds.y + 42);
            AllGuiTextures.JEI_DOWN_ARROW.render(graphics, bounds.x + 78, bounds.y + 9);
            EntryStack<FluidStack> slot = fluidSlot.getCurrentEntry().cast();
            graphics.state.addSpecialElement(new DrainRenderState(
                new Matrix3x2f(graphics.getMatrices()),
                slot.getValue().getFluid(),
                bounds.x + 80,
                bounds.y + 28
            ));
        }));
        widgets.add(createInputSlot(input).entries(display.input()));
        widgets.add(fluidSlot);
        widgets.add(createInputSlot(result).entries(display.result()));
    }

    @Override
    public int getDisplayHeight() {
        return 60;
    }
}
