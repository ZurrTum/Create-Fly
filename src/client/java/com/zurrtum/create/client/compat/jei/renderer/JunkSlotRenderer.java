package com.zurrtum.create.client.compat.jei.renderer;

import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JunkSlotRenderer implements IIngredientRenderer<ItemStack> {
    private static final JunkSlotRenderer INSTANCE = new JunkSlotRenderer();

    public static IRecipeSlotBuilder addSlot(IRecipeLayoutBuilder builder, int x, int y) {
        return builder.addSlot(RecipeIngredientRole.RENDER_ONLY, x, y).setCustomRenderer(VanillaTypes.ITEM_STACK, INSTANCE)
            .add(Items.BARRIER.getDefaultInstance());
    }

    @Override
    public void render(GuiGraphics graphics, ItemStack temp) {
        AllGuiTextures.JEI_CHANCE_SLOT.render(graphics, -1, -1);
        Component text = Component.literal("?").withStyle(ChatFormatting.BOLD);
        Font textRenderer = graphics.minecraft.font;
        graphics.drawString(textRenderer, text, textRenderer.width(text) / -2 + 7, 4, 0xffefefef, true);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, ItemStack ingredient, TooltipFlag tooltipFlag) {
        tooltip.clear();
    }

    @Override
    @NotNull
    public List<Component> getTooltip(ItemStack temp, TooltipFlag tooltipFlag) {
        return List.of();
    }
}
