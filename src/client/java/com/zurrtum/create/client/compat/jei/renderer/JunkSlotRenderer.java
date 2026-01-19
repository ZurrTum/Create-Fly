package com.zurrtum.create.client.compat.jei.renderer;

import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

public class JunkSlotRenderer implements IIngredientRenderer<ItemStack> {
    private static final JunkSlotRenderer INSTANCE = new JunkSlotRenderer();

    public static void addSlot(IRecipeLayoutBuilder builder, int x, int y, float chance) {
        ItemStack temp = Items.BARRIER.getDefaultInstance();
        CompoundTag nbt = new CompoundTag();
        nbt.putFloat("chance", chance);
        CustomData.set(DataComponents.CUSTOM_DATA, temp, nbt);
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, x, y).setCustomRenderer(VanillaTypes.ITEM_STACK, INSTANCE).add(temp);
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
        IIngredientRenderer.super.getTooltip(tooltip, ingredient, tooltipFlag);
    }

    @Override
    public List<Component> getTooltip(ItemStack temp, TooltipFlag tooltipFlag) {
        float chance = temp.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getFloatOr("chance", 0);
        String number = chance < 0.01 ? "<1" : chance > 0.99 ? ">99" : String.valueOf(Math.round(chance * 100));
        return List.of(
            CreateLang.translateDirect("recipe.assembly.junk"),
            CreateLang.translateDirect("recipe.processing.chance", number).withStyle(ChatFormatting.GOLD)
        );
    }
}
