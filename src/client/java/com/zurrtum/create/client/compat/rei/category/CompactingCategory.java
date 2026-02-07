package com.zurrtum.create.client.compat.rei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.rei.CreateCategory;
import com.zurrtum.create.client.compat.rei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.BasinBlazeBurnerRenderState;
import com.zurrtum.create.client.foundation.gui.render.PressBasinRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.compat.rei.display.CompactingDisplay;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class CompactingCategory extends CreateCategory<CompactingDisplay> {
    @Override
    public CategoryIdentifier<? extends CompactingDisplay> getCategoryIdentifier() {
        return ReiCommonPlugin.PACKING;
    }

    @Override
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.packing");
    }

    @Override
    public Renderer getIcon() {
        return new TwoIconRenderer(AllItems.MECHANICAL_PRESS, AllItems.BASIN);
    }

    @Override
    public void addWidgets(List<Widget> widgets, CompactingDisplay display, Rectangle bounds) {
        List<EntryIngredient> ingredients = display.inputs();
        List<Point> points = new ArrayList<>();
        List<Point> outputs = new ArrayList<>();
        List<EntryIngredient> outputIngredients = new ArrayList<>();
        List<Point> chances = new ArrayList<>();
        List<EntryIngredient> chanceIngredients = new ArrayList<>();
        List<ProcessingOutput> results = display.outputs();
        for (int i = 0, size = ingredients.size(), xOffset = bounds.x + 17 + (size < 3 ? (3 - size) * 19 / 2 : 0), yOffset = bounds.y + (size <= 9 ? 56 : 65); i < size; i++) {
            points.add(new Point(xOffset + (i % 3) * 19, yOffset - (i / 3) * 19));
        }
        int resultSize = results.size();
        boolean isOddSize = resultSize % 2 != 0;
        for (int i = 0, end = resultSize - 1, start = bounds.x, y = bounds.y + 56, xPosition, yPosition; i < resultSize; i++) {
            if (isOddSize && i == end) {
                xPosition = start + 147;
            } else {
                xPosition = start + (i % 2 == 0 ? 137 : 156);
            }
            yPosition = -19 * (i / 2) + y;
            addOutputData(results.get(i), xPosition, yPosition, outputs, outputIngredients, chances, chanceIngredients);
        }
        HeatCondition requiredHeat = display.heat();
        widgets.add(Widgets.createDrawableWidget((DrawContext graphics, int mouseX, int mouseY, float delta) -> {
            drawSlotBackground(graphics, points);
            drawSlotBackground(graphics, outputs);
            drawChanceSlotBackground(graphics, chances);
            AllGuiTextures.JEI_DOWN_ARROW.render(graphics, bounds.x + 141, bounds.y + (resultSize <= 4 ? 37 : 46) - (resultSize - 1) / 2 * 19);
            Matrix3x2f pose = new Matrix3x2f(graphics.getMatrices());
            if (requiredHeat == HeatCondition.NONE) {
                AllGuiTextures.JEI_NO_HEAT_BAR.render(graphics, bounds.x + 9, bounds.y + 85);
                AllGuiTextures.JEI_SHADOW.render(graphics, bounds.x + 86, bounds.y + 73);
            } else {
                AllGuiTextures.JEI_HEAT_BAR.render(graphics, bounds.x + 9, bounds.y + 85);
                AllGuiTextures.JEI_LIGHT.render(graphics, bounds.x + 86, bounds.y + 93);
                graphics.state.addSpecialElement(new BasinBlazeBurnerRenderState(
                    pose,
                    bounds.x + 96,
                    bounds.y + 74,
                    requiredHeat.visualizeAsBlazeBurner()
                ));
            }
            graphics.state.addSpecialElement(new PressBasinRenderState(pose, bounds.x + 96, bounds.y));
            graphics.drawText(
                graphics.client.textRenderer,
                CreateLang.translateDirect(requiredHeat.getTranslationKey()),
                bounds.x + 14,
                bounds.y + 91,
                requiredHeat.getColor(),
                false
            );
        }));
        for (int i = 0, size = points.size(); i < size; i++) {
            widgets.add(createInputSlot(points.get(i)).entries(getRenderEntryStack(ingredients.get(i))));
        }
        for (int i = 0, size = outputs.size(); i < size; i++) {
            widgets.add(createOutputSlot(outputs.get(i)).entries(outputIngredients.get(i)));
        }
        for (int i = 0, size = chances.size(); i < size; i++) {
            widgets.add(createOutputSlot(chances.get(i)).entries(chanceIngredients.get(i)));
        }
        if (!requiredHeat.testBlazeBurner(BlazeBurnerBlock.HeatLevel.NONE)) {
            widgets.add(createSlot(new Point(bounds.x + 139, bounds.y + 86)).entries(EntryIngredients.of(AllItems.BLAZE_BURNER)));
        }
        if (!requiredHeat.testBlazeBurner(BlazeBurnerBlock.HeatLevel.KINDLED)) {
            widgets.add(createSlot(new Point(bounds.x + 158, bounds.y + 86)).entries(EntryIngredients.of(AllItems.BLAZE_CAKE)));
        }
    }

    @Override
    public int getDisplayHeight() {
        return 113;
    }
}