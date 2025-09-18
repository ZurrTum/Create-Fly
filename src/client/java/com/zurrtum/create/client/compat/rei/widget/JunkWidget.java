package com.zurrtum.create.client.compat.rei.widget;

import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class JunkWidget extends Widget {
    private final Rectangle bounds;
    private final float chance;

    private final NumberAnimator<Float> darkHighlightedAlpha = ValueAnimator.ofFloat()
        .withConvention(() -> REIRuntime.getInstance().isDarkThemeEnabled() ? 1.0F : 0.0F, ValueAnimator.typicalTransitionTime()).asFloat();

    public JunkWidget(int x, int y, float chance) {
        this.bounds = new Rectangle(x, y, 16, 16);
        this.chance = chance;
    }

    @Override
    public List<? extends Element> children() {
        return List.of();
    }

    @Override
    public void render(DrawContext graphics, int mouseX, int mouseY, float delta) {
        darkHighlightedAlpha.update(delta);
        AllGuiTextures.JEI_CHANCE_SLOT.render(graphics, bounds.x - 1, bounds.y - 1);
        Text text = Text.literal("?").formatted(Formatting.BOLD);
        graphics.drawText(font, text, bounds.x + font.getWidth(text) / -2 + 7, bounds.y + 4, 0xffefefef, true);
        if (bounds.contains(mouseX, mouseY)) {
            graphics.fillGradient(bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), 0x80ffffff, 0x80ffffff);
            int darkColor = 0x111111 | ((int) (90 * darkHighlightedAlpha.value()) << 24);
            graphics.fillGradient(bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), darkColor, darkColor);
            String number = chance < 0.01 ? "<1" : chance > 0.99 ? ">99" : String.valueOf(Math.round(chance * 100));
            Tooltip.create(
                CreateLang.translateDirect("recipe.assembly.junk"),
                CreateLang.translateDirect("recipe.processing.chance", number).formatted(Formatting.GOLD)
            ).queue();
        }
    }
}
