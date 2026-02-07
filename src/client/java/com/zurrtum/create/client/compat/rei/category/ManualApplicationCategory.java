package com.zurrtum.create.client.compat.rei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.rei.CreateCategory;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.ManualBlockRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.compat.rei.display.ManualApplicationDisplay;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class ManualApplicationCategory extends CreateCategory<ManualApplicationDisplay> {
    @Override
    public CategoryIdentifier<? extends ManualApplicationDisplay> getCategoryIdentifier() {
        return ReiCommonPlugin.ITEM_APPLICATION;
    }

    @Override
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.item_application");
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(AllItems.BRASS_HAND);
    }

    @Override
    public void addWidgets(List<Widget> widgets, ManualApplicationDisplay display, Rectangle bounds) {
        Point input = new Point(bounds.x + 56, bounds.y + 10);
        Point target = new Point(bounds.x + 32, bounds.y + 43);
        List<Point> outputs = new ArrayList<>();
        List<EntryIngredient> outputIngredients = new ArrayList<>();
        List<Point> chances = new ArrayList<>();
        List<EntryIngredient> chanceIngredients = new ArrayList<>();
        List<ProcessingOutput> results = display.outputs();
        for (int i = 0, size = results.size(), start = bounds.x + 137, y = bounds.y + 43; i < size; i++) {
            addOutputData(results.get(i), i % 2 == 0 ? start : start + 19, y + (i / 2) * -19, outputs, outputIngredients, chances, chanceIngredients);
        }
        Slot targetSlot = createInputSlot(target).entries(display.target());
        widgets.add(Widgets.createDrawableWidget((DrawContext graphics, int mouseX, int mouseY, float delta) -> {
            drawSlotBackground(graphics, outputs, input, target);
            drawChanceSlotBackground(graphics, chances);
            AllGuiTextures.JEI_SHADOW.render(graphics, bounds.x + 67, bounds.y + 52);
            AllGuiTextures.JEI_DOWN_ARROW.render(graphics, bounds.x + 79, bounds.y + 15);
            EntryStack<ItemStack> slot = targetSlot.getCurrentEntry().cast();
            ItemStack stack = slot.getValue();
            if (stack.getItem() instanceof BlockItem blockItem) {
                BlockState block = blockItem.getBlock().getDefaultState();
                graphics.state.addSpecialElement(new ManualBlockRenderState(
                    new Matrix3x2f(graphics.getMatrices()),
                    block,
                    bounds.x + 79,
                    bounds.y + 34
                ));
            }
        }));
        widgets.add(createInputSlot(input).entries(getKeepHeldStack(display.input(), display.keepHeldItem())));
        widgets.add(targetSlot);
        for (int i = 0, size = outputs.size(); i < size; i++) {
            widgets.add(createOutputSlot(outputs.get(i)).entries(outputIngredients.get(i)));
        }
        for (int i = 0, size = chances.size(); i < size; i++) {
            widgets.add(createOutputSlot(chances.get(i)).entries(chanceIngredients.get(i)));
        }
    }

    @Override
    public int getDisplayHeight() {
        return 70;
    }
}
