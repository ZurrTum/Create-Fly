package com.zurrtum.create.client.compat.rei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.rei.CreateCategory;
import com.zurrtum.create.client.compat.rei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.FanRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.compat.rei.display.FanBlastingDisplay;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.joml.Matrix3x2f;

import java.util.List;

public class FanBlastingCategory extends CreateCategory<FanBlastingDisplay> {
    @Override
    public CategoryIdentifier<? extends FanBlastingDisplay> getCategoryIdentifier() {
        return ReiCommonPlugin.FAN_BLASTING;
    }

    @Override
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.fan_blasting");
    }

    @Override
    public Renderer getIcon() {
        return new TwoIconRenderer(AllItems.PROPELLER, Items.LAVA_BUCKET);
    }

    @Override
    public void addWidgets(List<Widget> widgets, FanBlastingDisplay display, Rectangle bounds) {
        Point input = new Point(bounds.x + 26, bounds.y + 53);
        Point output = new Point(bounds.x + 146, bounds.y + 53);
        widgets.add(Widgets.createDrawableWidget((DrawContext graphics, int mouseX, int mouseY, float delta) -> {
            drawSlotBackground(graphics, input, output);
            AllGuiTextures.JEI_SHADOW.render(graphics, bounds.x + 51, bounds.y + 32);
            AllGuiTextures.JEI_LIGHT.render(graphics, bounds.x + 70, bounds.y + 44);
            AllGuiTextures.JEI_LONG_ARROW.render(graphics, bounds.x + 59, bounds.y + 56);
            graphics.state.addSpecialElement(new FanRenderState(
                new Matrix3x2f(graphics.getMatrices()),
                bounds.x + 61,
                bounds.y + 10,
                Fluids.LAVA.getDefaultState().getBlockState()
            ));
        }));
        widgets.add(createInputSlot(input).entries(display.input()));
        widgets.add(createOutputSlot(output).entries(display.output()));
    }

    @Override
    public int getDisplayHeight() {
        return 82;
    }
}
