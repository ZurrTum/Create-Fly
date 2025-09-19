package com.zurrtum.create.client.compat.rei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.rei.CreateCategory;
import com.zurrtum.create.client.compat.rei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.SpoutRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.compat.rei.display.SpoutFillingDisplay;
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
import java.util.concurrent.atomic.AtomicInteger;

public class SpoutFillingCategory extends CreateCategory<SpoutFillingDisplay> {
    public static final int MAX = 3;
    public static AtomicInteger idGenerator = new AtomicInteger();

    @Override
    public CategoryIdentifier<? extends SpoutFillingDisplay> getCategoryIdentifier() {
        return ReiCommonPlugin.SPOUT_FILLING;
    }

    @Override
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.spout_filling");
    }

    @Override
    public Renderer getIcon() {
        return new TwoIconRenderer(AllItems.SPOUT, Items.WATER_BUCKET);
    }

    @Override
    public void addWidgets(List<Widget> widgets, SpoutFillingDisplay display, Rectangle bounds) {
        Point input = new Point(bounds.x + 32, bounds.y + 56);
        Point fluid = new Point(bounds.x + 32, bounds.y + 37);
        Point output = new Point(bounds.x + 137, bounds.y + 56);
        Slot fluidSlot = createInputSlot(fluid).entries(getRenderEntryStack(display.fluid()));
        widgets.add(Widgets.createDrawableWidget((DrawContext graphics, int mouseX, int mouseY, float delta) -> {
            drawSlotBackground(graphics, input, fluid, output);
            AllGuiTextures.JEI_SHADOW.render(graphics, bounds.x + 67, bounds.y + 62);
            AllGuiTextures.JEI_DOWN_ARROW.render(graphics, bounds.x + 131, bounds.y + 34);
            EntryStack<FluidStack> slot = fluidSlot.getCurrentEntry().cast();
            int i = idGenerator.getAndIncrement();
            if (i >= MAX) {
                idGenerator.set(0);
            }
            graphics.state.addSpecialElement(new SpoutRenderState(
                i,
                new Matrix3x2f(graphics.getMatrices()),
                slot.getValue().getFluid(),
                bounds.x + 80,
                bounds.y + 6,
                0
            ));
        }));
        widgets.add(createInputSlot(input).entries(display.input()));
        widgets.add(fluidSlot);
        widgets.add(createOutputSlot(output).entries(display.output()));
    }

    @Override
    public int getDisplayHeight() {
        return 80;
    }
}
