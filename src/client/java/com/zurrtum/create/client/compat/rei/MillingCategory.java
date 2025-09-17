package com.zurrtum.create.client.compat.rei;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.MillstoneRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.rei.MillingDisplay;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.content.processing.recipe.ChanceOutput;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class MillingCategory extends CreateCategory<MillingDisplay> {
    @Override
    public CategoryIdentifier<? extends MillingDisplay> getCategoryIdentifier() {
        return ReiCommonPlugin.MILLING;
    }

    @Override
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.milling");
    }

    @Override
    public Renderer getIcon() {
        return new TwoIconRenderer(AllItems.MILLSTONE, AllItems.WHEAT_FLOUR);
    }

    @Override
    void addWidgets(List<Widget> widgets, MillingDisplay display, Rectangle bounds) {
        Point input = new Point(bounds.x + 20, bounds.y + 14);
        List<Point> outputs = new ArrayList<>();
        List<EntryIngredient> outputIngredients = new ArrayList<>();
        List<Point> chances = new ArrayList<>();
        List<EntryIngredient> chanceIngredients = new ArrayList<>();
        List<ChanceOutput> results = display.outputs();
        int outputSize = results.size();
        if (outputSize == 1) {
            addOutputData(results.getFirst(), bounds.x + 144, bounds.y + 32, outputs, outputIngredients, chances, chanceIngredients);
        } else {
            for (int i = 0; i < outputSize; i++) {
                int xOffset = i % 2 == 0 ? 0 : 19;
                int yOffset = (i / 2) * -19;
                addOutputData(
                    results.get(i),
                    bounds.x + 138 + xOffset,
                    bounds.y + 32 + yOffset,
                    outputs,
                    outputIngredients,
                    chances,
                    chanceIngredients
                );
            }
        }
        widgets.add(Widgets.createDrawableWidget((DrawContext graphics, int mouseX, int mouseY, float delta) -> {
            drawSlotBackground(graphics, outputs, input);
            drawChanceSlotBackground(graphics, chances);
            AllGuiTextures.JEI_ARROW.render(graphics, bounds.x + 90, bounds.y + 37);
            AllGuiTextures.JEI_DOWN_ARROW.render(graphics, bounds.x + 48, bounds.y + 9);
            AllGuiTextures.JEI_SHADOW.render(graphics, bounds.x + 37, bounds.y + 45);
            graphics.state.addSpecialElement(new MillstoneRenderState(new Matrix3x2f(graphics.getMatrices()), bounds.x + 47, bounds.y + 24));
        }));
        widgets.add(createInputSlot(input).entries(display.input()));
        for (int i = 0, size = outputs.size(); i < size; i++) {
            widgets.add(createOutputSlot(outputs.get(i)).entries(outputIngredients.get(i)));
        }
        for (int i = 0, size = chances.size(); i < size; i++) {
            widgets.add(createOutputSlot(chances.get(i)).entries(chanceIngredients.get(i)));
        }
    }

    private void addOutputData(
        ChanceOutput output,
        int x,
        int y,
        List<Point> outputs,
        List<EntryIngredient> outputIngredients,
        List<Point> chances,
        List<EntryIngredient> chanceIngredients
    ) {
        float chance = output.chance();
        Point point = new Point(x, y);
        if (chance == 1) {
            outputs.add(point);
            outputIngredients.add(EntryIngredients.of(output.stack()));
        } else {
            chances.add(point);
            EntryStack<ItemStack> stack = EntryStacks.of(output.stack());
            stack.withRenderer(new ChanceItemRender(chance, stack.getRenderer()));
            chanceIngredients.add(EntryIngredient.of(stack));
        }
    }

    @Override
    public int getDisplayHeight() {
        return 63;
    }
}
