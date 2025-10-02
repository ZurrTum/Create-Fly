package com.zurrtum.create.client.compat.rei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.rei.CreateCategory;
import com.zurrtum.create.client.compat.rei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.SawRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.compat.rei.display.BlockCuttingDisplay;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.joml.Matrix3x2f;

import java.util.List;

public class BlockCuttingCategory extends CreateCategory<BlockCuttingDisplay> {
    @Override
    public CategoryIdentifier<? extends BlockCuttingDisplay> getCategoryIdentifier() {
        return ReiCommonPlugin.BLOCK_CUTTING;
    }

    @Override
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.block_cutting");
    }

    @Override
    public Renderer getIcon() {
        return new TwoIconRenderer(AllItems.MECHANICAL_SAW, Items.STONE_BRICK_STAIRS);
    }

    @Override
    public void addWidgets(List<Widget> widgets, BlockCuttingDisplay display, Rectangle bounds) {
        Point input = new Point(bounds.x + 10, bounds.y + 10);
        List<EntryIngredient> outputIngredients = display.outputs();
        int size = outputIngredients.size();
        Point[] outputs = new Point[size];
        for (int i = 0, left = bounds.x + 83, top = bounds.y + 53; i < size; i++) {
            outputs[i] = new Point(left + (i % 5) * 19, top + (i / 5) * -19);
        }
        widgets.add(Widgets.createDrawableWidget((DrawContext graphics, int mouseX, int mouseY, float delta) -> {
            drawSlotBackground(graphics, input);
            drawSlotBackground(graphics, outputs);
            AllGuiTextures.JEI_DOWN_ARROW.render(graphics, bounds.x + 36, bounds.y + 11);
            AllGuiTextures.JEI_SHADOW.render(graphics, bounds.x + 21, bounds.y + 55);
            graphics.state.addSpecialElement(new SawRenderState(new Matrix3x2f(graphics.getMatrices()), bounds.x + 30, bounds.y + 31));
        }));
        widgets.add(createInputSlot(input).entries(display.input()));
        for (int i = 0; i < size; i++) {
            widgets.add(createOutputSlot(outputs[i]).entries(outputIngredients.get(i)));
        }
    }

    @Override
    public int getDisplayHeight() {
        return 80;
    }
}
