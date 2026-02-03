package com.zurrtum.create.client.compat.rei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.rei.CreateCategory;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.DeployerRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.compat.rei.display.DeployingDisplay;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class DeployingCategory extends CreateCategory<DeployingDisplay> {
    @Override
    public CategoryIdentifier<? extends DeployingDisplay> getCategoryIdentifier() {
        return ReiCommonPlugin.DEPLOYING;
    }

    @Override
    public Component getTitle() {
        return CreateLang.translateDirect("recipe.deploying");
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(AllItems.DEPLOYER);
    }

    @Override
    public void addWidgets(List<Widget> widgets, DeployingDisplay display, Rectangle bounds) {
        Point input = new Point(bounds.x + 56, bounds.y + 10);
        Point target = new Point(bounds.x + 32, bounds.y + 56);
        List<Point> outputs = new ArrayList<>();
        List<EntryIngredient> outputIngredients = new ArrayList<>();
        List<Point> chances = new ArrayList<>();
        List<EntryIngredient> chanceIngredients = new ArrayList<>();
        List<ProcessingOutput> results = display.outputs();
        for (int i = 0, size = results.size(), start = bounds.x + 137, y = bounds.y + 56; i < size; i++) {
            addOutputData(results.get(i), i % 2 == 0 ? start : start + 19, y + (i / 2) * -19, outputs, outputIngredients, chances, chanceIngredients);
        }
        widgets.add(Widgets.createDrawableWidget((GuiGraphics graphics, int mouseX, int mouseY, float delta) -> {
            drawSlotBackground(graphics, outputs, input, target);
            drawChanceSlotBackground(graphics, chances);
            AllGuiTextures.JEI_SHADOW.render(graphics, bounds.x + 67, bounds.y + 62);
            AllGuiTextures.JEI_DOWN_ARROW.render(graphics, bounds.x + 131, bounds.y + (results.size() <= 2 ? 34 : 15));
            graphics.guiRenderState.submitPicturesInPictureState(new DeployerRenderState(
                new Matrix3x2f(graphics.pose()),
                bounds.x + 80,
                bounds.y - 5
            ));
        }));
        widgets.add(createInputSlot(input).entries(getKeepHeldStack(display.input(), display.keepHeldItem())));
        widgets.add(createInputSlot(target).entries(display.target()));
        for (int i = 0, size = outputs.size(); i < size; i++) {
            widgets.add(createOutputSlot(outputs.get(i)).entries(outputIngredients.get(i)));
        }
        for (int i = 0, size = chances.size(); i < size; i++) {
            widgets.add(createOutputSlot(chances.get(i)).entries(chanceIngredients.get(i)));
        }
    }

    @Override
    public int getDisplayHeight() {
        return 80;
    }
}
