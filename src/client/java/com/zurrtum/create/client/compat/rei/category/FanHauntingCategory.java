package com.zurrtum.create.client.compat.rei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.rei.CreateCategory;
import com.zurrtum.create.client.compat.rei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.FanRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.compat.rei.display.FanHauntingDisplay;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class FanHauntingCategory extends CreateCategory<FanHauntingDisplay> {
    @Override
    public CategoryIdentifier<? extends FanHauntingDisplay> getCategoryIdentifier() {
        return ReiCommonPlugin.FAN_HAUNTING;
    }

    @Override
    public Component getTitle() {
        return CreateLang.translateDirect("recipe.fan_haunting");
    }

    @Override
    public Renderer getIcon() {
        return new TwoIconRenderer(AllItems.PROPELLER, Items.SOUL_CAMPFIRE);
    }

    @Override
    public void addWidgets(List<Widget> widgets, FanHauntingDisplay display, Rectangle bounds) {
        List<Point> outputs = new ArrayList<>();
        List<EntryIngredient> outputIngredients = new ArrayList<>();
        List<Point> chances = new ArrayList<>();
        List<EntryIngredient> chanceIngredients = new ArrayList<>();
        List<ProcessingOutput> results = display.outputs();
        int outputSize = results.size();
        Point input;
        int xOffsetAmount = 1 - Math.min(3, outputSize);
        if (outputSize == 1) {
            input = new Point(bounds.x + 26, bounds.y + 53);
            addOutputData(results.getFirst(), bounds.x + 146, bounds.y + 53, outputs, outputIngredients, chances, chanceIngredients);
        } else {
            input = new Point(bounds.x + 26 + xOffsetAmount * 5, bounds.y + 53);
            for (int i = 0, left = bounds.x + 146 + xOffsetAmount * 9, top = bounds.y + 53; i < outputSize; i++) {
                int xOffset = (i % 3) * 19;
                int yOffset = (i / 3) * -19;
                addOutputData(results.get(i), left + xOffset, top + yOffset, outputs, outputIngredients, chances, chanceIngredients);
            }
        }
        widgets.add(Widgets.createDrawableWidget((GuiGraphics graphics, int mouseX, int mouseY, float delta) -> {
            drawSlotBackground(graphics, outputs, input);
            drawChanceSlotBackground(graphics, chances);
            AllGuiTextures.JEI_SHADOW.render(graphics, bounds.x + 51, bounds.y + 32);
            AllGuiTextures.JEI_LIGHT.render(graphics, bounds.x + 70, bounds.y + 44);
            AllGuiTextures.JEI_LONG_ARROW.render(graphics, bounds.x + 59 + 7 * xOffsetAmount, bounds.y + 56);
            graphics.guiRenderState.submitPicturesInPictureState(new FanRenderState(
                new Matrix3x2f(graphics.pose()),
                bounds.x + 61,
                bounds.y + 9,
                Blocks.SOUL_FIRE.defaultBlockState()
            ));
        }));
        widgets.add(createInputSlot(input).entries(display.input()));
        for (int i = 0, size = outputs.size(); i < size; i++) {
            widgets.add(createOutputSlot(outputs.get(i)).entries(outputIngredients.get(i)));
        }
        for (int i = 0, size = chances.size(); i < size; i++) {
            widgets.add(createOutputSlot(chances.get(i)).entries(chanceIngredients.get(i)));
        }
    }

    @Override
    public int getDisplayHeight() {
        return 82;
    }
}
