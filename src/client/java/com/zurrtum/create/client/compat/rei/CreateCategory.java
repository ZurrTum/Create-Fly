package com.zurrtum.create.client.compat.rei;

import com.zurrtum.create.client.compat.rei.renderer.ChanceItemRenderer;
import com.zurrtum.create.client.compat.rei.renderer.FluidStackRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.content.processing.recipe.ChanceOutput;
import dev.architectury.fluid.FluidStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class CreateCategory<T extends Display> implements DisplayCategory<T> {
    public abstract void addWidgets(List<Widget> widgets, T display, Rectangle bounds);

    public static void drawSlotBackground(DrawContext graphics, List<Point> points1, Point... points2) {
        for (Point point : points1) {
            AllGuiTextures.JEI_SLOT.render(graphics, point.x - 1, point.y - 1);
        }
        drawSlotBackground(graphics, points2);
    }

    public static void drawSlotBackground(DrawContext graphics, Point... points) {
        for (Point point : points) {
            AllGuiTextures.JEI_SLOT.render(graphics, point.x - 1, point.y - 1);
        }
    }

    public static void drawChanceSlotBackground(DrawContext graphics, List<Point> points) {
        for (Point point : points) {
            AllGuiTextures.JEI_CHANCE_SLOT.render(graphics, point.x - 1, point.y - 1);
        }
    }

    public static void drawChanceSlotBackground(DrawContext graphics, Point... points) {
        for (Point point : points) {
            AllGuiTextures.JEI_CHANCE_SLOT.render(graphics, point.x - 1, point.y - 1);
        }
    }

    public static Slot createSlot(Point point) {
        return Widgets.createSlot(point).disableBackground();
    }

    public static Slot createInputSlot(Point point) {
        return Widgets.createSlot(point).markInput().disableBackground();
    }

    public static Slot createOutputSlot(Point point) {
        return Widgets.createSlot(point).markOutput().disableBackground();
    }

    public static EntryIngredient getRenderEntryStack(EntryIngredient ingredient) {
        if (ingredient.getFirst().getValue() instanceof FluidStack) {
            for (EntryStack<FluidStack> stack : ingredient.<FluidStack>castAsList()) {
                EntryRenderer<FluidStack> renderer = stack.getRenderer();
                if (renderer instanceof FluidStackRenderer) {
                    continue;
                }
                stack.withRenderer(new FluidStackRenderer(renderer));
            }
        }
        return ingredient;
    }

    public static EntryIngredient getRenderEntryStack(ChanceOutput output) {
        float chance = output.chance();
        if (chance == 1) {
            return EntryIngredients.of(output.stack());
        } else {
            EntryStack<ItemStack> stack = EntryStacks.of(output.stack());
            stack.withRenderer(new ChanceItemRenderer(chance, stack.getRenderer()));
            return EntryIngredient.of(stack);
        }
    }

    public static List<EntryIngredient> condenseIngredients(List<EntryIngredient> ingredients) {
        List<ItemStack> cache = new ArrayList<>();
        List<EntryIngredient> result = new ArrayList<>();
        Find:
        for (EntryIngredient ingredient : ingredients) {
            if (ingredient.isEmpty()) {
                continue;
            }
            int size = ingredient.size();
            if (size != 1) {
                result.add(ingredient);
                continue;
            }
            EntryStack<?> entryStack = ingredient.getFirst();
            if (!(entryStack.getValue() instanceof ItemStack stack)) {
                result.add(ingredient);
                continue;
            }
            for (ItemStack target : cache) {
                if (ItemStack.areItemsAndComponentsEqual(stack, target)) {
                    target.increment(stack.getCount());
                    continue Find;
                }
            }
            stack = stack.copy();
            cache.add(stack);
            result.add(EntryIngredients.of(stack));
        }
        return result;
    }

    public static void addOutputData(
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
        EntryIngredient ingredient = getRenderEntryStack(output);
        if (chance == 1) {
            outputs.add(point);
            outputIngredients.add(ingredient);
        } else {
            chances.add(point);
            chanceIngredients.add(ingredient);
        }
    }

    @Override
    public List<Widget> setupDisplay(T display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));
        addWidgets(widgets, display, bounds);
        return widgets;
    }

    @Override
    public int getDisplayWidth(T display) {
        return 187;
    }
}
