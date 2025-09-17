package com.zurrtum.create.client.compat.rei;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.rei.MechanicalCraftingDisplay;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MechanicalCraftingCategory extends CreateCategory<MechanicalCraftingDisplay> {
    @Override
    public CategoryIdentifier<? extends MechanicalCraftingDisplay> getCategoryIdentifier() {
        return ReiCommonPlugin.MECHANICAL_CRAFTING;
    }

    @Override
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.mechanical_crafting");
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(AllItems.MECHANICAL_CRAFTER);
    }

    @Override
    void addWidgets(List<Widget> widgets, MechanicalCraftingDisplay display, Rectangle bounds) {
        int width = display.width();
        int height = display.height();
        List<Optional<Ingredient>> layout = display.inputs();
        List<Point> inputs = new ArrayList<>();
        List<EntryIngredient> ingredients = new ArrayList<>();
        int left = bounds.x + 12;
        if (width < 5) {
            left += (19 * (5 - width)) / 2;
        }
        int top = bounds.y + 12;
        if (height < 5) {
            top += (19 * (5 - height)) / 2;
        }
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                Optional<Ingredient> ingredient = layout.get(h * width + w);
                if (ingredient.isEmpty()) {
                    continue;
                }
                inputs.add(new Point(left + 16 * w + (w * 3), top + 16 * h + (h * 3)));
                ingredients.add(EntryIngredients.ofIngredient(ingredient.get()));
            }
        }
        Point output = new Point(bounds.x + 138, bounds.y + 85);
        widgets.add(Widgets.createDrawableWidget((DrawContext graphics, int mouseX, int mouseY, float delta) -> {
            drawSlotBackground(graphics, inputs, output);
            AllGuiTextures.JEI_DOWN_ARROW.render(graphics, bounds.x + 133, bounds.y + 64);
            AllGuiTextures.JEI_SHADOW.render(graphics, bounds.x + 118, bounds.y + 43);
            graphics.drawText(
                MinecraftClient.getInstance().textRenderer,
                String.valueOf(ingredients.size()),
                bounds.x + 147,
                bounds.y + 44,
                0xFFFFFFFF,
                true
            );
        }));
        for (int i = 0; i < inputs.size(); i++) {
            widgets.add(createInputSlot(inputs.get(i)).entries(ingredients.get(i)));
        }
        widgets.add(createOutputSlot(output).entries(display.output()));
    }

    @Override
    public int getDisplayHeight() {
        return 117;
    }
}
