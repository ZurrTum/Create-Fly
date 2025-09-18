package com.zurrtum.create.client.compat.rei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.rei.CreateCategory;
import com.zurrtum.create.client.compat.rei.widget.JunkWidget;
import com.zurrtum.create.client.compat.rei.widget.TooltipWidget;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.render.PressRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.compat.rei.display.SequencedAssemblyDisplay;
import com.zurrtum.create.compat.rei.display.SequencedAssemblyDisplay.SequenceData;
import com.zurrtum.create.content.processing.recipe.ChanceOutput;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.recipe.RecipeType;
import net.minecraft.text.Text;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class SequencedAssemblyCategory extends CreateCategory<SequencedAssemblyDisplay> {
    public static String[] ROMANS = {"I", "II", "III", "IV", "V", "VI", "-"};
    public static Map<RecipeType<?>, TriConsumer<DrawContext, Integer, Point>> DRAW = new IdentityHashMap<>();

    static {
        DRAW.put(
            AllRecipeTypes.PRESSING, (graphics, i, point) -> {
                float scale = 19 / 30f;
                Matrix3x2fStack matrices = graphics.getMatrices();
                matrices.pushMatrix();
                matrices.translate(point.x, point.y);
                matrices.scale(scale, scale);
                matrices.translate(-point.x, -point.y);
                graphics.state.addSpecialElement(new PressRenderState(i, new Matrix3x2f(matrices), point.x - 3, point.y + 18, i));
                matrices.popMatrix();
            }
        );
    }

    public static void registerDraw(RecipeType<?> type, TriConsumer<DrawContext, Integer, Point> draw) {
        DRAW.put(type, draw);
    }

    @Override
    public CategoryIdentifier<? extends SequencedAssemblyDisplay> getCategoryIdentifier() {
        return ReiCommonPlugin.SEQUENCED_ASSEMBLY;
    }

    @Override
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.sequenced_assembly");
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(AllItems.PRECISION_MECHANISM);
    }

    @Override
    public void addWidgets(List<Widget> widgets, SequencedAssemblyDisplay display, Rectangle bounds) {
        ChanceOutput chanceOutput = display.output();
        boolean randomOutput = chanceOutput.chance() != 1;
        boolean willRepeat = display.loop() > 1;
        int xOffset = randomOutput ? bounds.x - 7 : bounds.x;
        Point input = new Point(xOffset + 32, bounds.y + 96);
        Point output = new Point(xOffset + 137, bounds.y + 96);
        SequenceData sequences = display.sequences();
        List<EntryIngredient> ingredients = sequences.ingredients();
        List<List<Text>> tooltips = sequences.tooltip();
        List<RecipeType<?>> types = sequences.types();
        List<Point> points = new ArrayList<>();
        int size = ingredients.size();
        boolean[] noBackground = new boolean[size];
        for (int i = 0, left = bounds.x + 99 - 14 * size, top = bounds.y; i < size; i++) {
            points.add(new Point(left + i * 28, top + 20));
            if (ingredients.get(i).isEmpty()) {
                noBackground[i] = true;
            }
        }
        widgets.add(Widgets.createDrawableWidget((DrawContext graphics, int mouseX, int mouseY, float delta) -> {
            TextRenderer textRenderer = graphics.client.textRenderer;
            for (int i = 0; i < size; i++) {
                Point point = points.get(i);
                String text = ROMANS[Math.min(i, ROMANS.length)];
                graphics.drawText(textRenderer, text, point.x + 8 - textRenderer.getWidth(text) / 2, point.y - 13, 0xff888888, false);
                TriConsumer<DrawContext, Integer, Point> draw = DRAW.get(types.get(i));
                if (draw != null) {
                    draw.accept(graphics, i, point);
                }
                if (noBackground[i]) {
                    continue;
                }
                AllGuiTextures.JEI_SLOT.render(graphics, point.x - 1, point.y - 1);
            }
            drawSlotBackground(graphics, input);
            if (randomOutput) {
                drawChanceSlotBackground(graphics, output);
            } else {
                drawSlotBackground(graphics, output);
            }
            AllGuiTextures.JEI_LONG_ARROW.render(graphics, xOffset + 57, bounds.y + 99);
            if (willRepeat) {
                AllIcons.I_SEQ_REPEAT.render(graphics, xOffset + 70, bounds.y + 104);
                Text repeat = Text.literal("x" + display.loop());
                graphics.drawText(textRenderer, repeat, xOffset + 86, bounds.y + 109, 0xff888888, false);
            }
        }));
        for (int i = 0; i < size; i++) {
            Point point = points.get(i);
            List<Text> step = tooltips.get(i);
            if (noBackground[i]) {
                widgets.add(new TooltipWidget(point.x, point.y - 14, 16, 86, step));
            } else {
                Slot slot = createInputSlot(point).disableTooltips().entries(getRenderEntryStack(ingredients.get(i)));
                widgets.add(new TooltipWidget(
                    point.x, point.y - 14, 16, 86, mc -> {
                    Tooltip tooltip = slot.getCurrentTooltip(TooltipContext.ofMouse(Item.TooltipContext.create(mc.world)));
                    if (tooltip == null) {
                        return Tooltip.create(step);
                    }
                    List<Tooltip.Entry> entries = tooltip.entries();
                    Tooltip.Entry first = entries.getFirst();
                    int len = step.size();
                    if (first.isText()) {
                        entries.set(0, Tooltip.entry(step.get(len - 1).copy().append(first.getAsText().getString())));
                    } else {
                        entries.addFirst(Tooltip.entry(step.get(len - 1)));
                    }
                    for (int j = len - 2; j >= 0; j--) {
                        entries.addFirst(Tooltip.entry(step.get(j)));
                    }
                    return tooltip;
                }
                ));
                widgets.add(slot);
            }
        }
        widgets.add(createInputSlot(input).entries(display.input()));
        widgets.add(createOutputSlot(output).entries(getRenderEntryStack(chanceOutput)));
        if (randomOutput) {
            widgets.add(new JunkWidget(xOffset + 156, bounds.y + 96, 1 - chanceOutput.chance()));
        }
        if (willRepeat) {
            widgets.add(new TooltipWidget(xOffset + 57, bounds.y + 99, 71, 18, CreateLang.translateDirect("recipe.assembly.repeat", display.loop())));
        }
    }

    @Override
    public int getDisplayHeight() {
        return 125;
    }
}
