package com.zurrtum.create.client.compat.rei;

import com.zurrtum.create.client.foundation.utility.CreateLang;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public record ChanceItemRender(float chance, EntryRenderer<ItemStack> origin) implements EntryRenderer<ItemStack> {
    @Override
    public void render(EntryStack<ItemStack> entry, DrawContext graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
        origin.render(entry, graphics, bounds, mouseX, mouseY, delta);
    }

    @Override
    public @Nullable Tooltip getTooltip(EntryStack<ItemStack> entry, TooltipContext context) {
        Tooltip tooltip = origin.getTooltip(entry, context);
        if (tooltip != null) {
            tooltip.add(CreateLang.translateDirect("recipe.processing.chance", chance < 0.01 ? "<1" : (int) (chance * 100))
                .formatted(Formatting.GOLD));
        }
        return tooltip;
    }
}
