package com.zurrtum.create.client.compat.jei.renderer;

import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JunkSlotRenderer implements IIngredientRenderer<ItemStack> {
    private static final JunkSlotRenderer INSTANCE = new JunkSlotRenderer();

    public static IRecipeSlotBuilder addSlot(IRecipeLayoutBuilder builder, int x, int y) {
        return builder.addSlot(RecipeIngredientRole.RENDER_ONLY, x, y).setCustomRenderer(VanillaTypes.ITEM_STACK, INSTANCE)
            .add(Items.BARRIER.getDefaultStack());
    }

    @Override
    public void render(DrawContext graphics, ItemStack temp) {
        AllGuiTextures.JEI_CHANCE_SLOT.render(graphics, -1, -1);
        Text text = Text.literal("?").formatted(Formatting.BOLD);
        TextRenderer textRenderer = graphics.client.textRenderer;
        graphics.drawText(textRenderer, text, textRenderer.getWidth(text) / -2 + 7, 4, 0xffefefef, true);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, ItemStack ingredient, TooltipType tooltipFlag) {
        tooltip.clear();
    }

    @Override
    @NotNull
    public List<Text> getTooltip(ItemStack temp, TooltipType tooltipFlag) {
        return List.of();
    }
}
